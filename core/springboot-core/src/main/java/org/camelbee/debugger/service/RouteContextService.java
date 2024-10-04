package org.camelbee.debugger.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.model.EnrichDefinition;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.PollEnrichDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.ProcessorDefinitionHelper;
import org.apache.camel.model.RecipientListDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutingSlipDefinition;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.model.ToDynamicDefinition;
import org.camelbee.debugger.model.route.CamelRoute;
import org.camelbee.debugger.model.route.CamelRouteOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * RouteContextService.
 */
@Component
public class RouteContextService {

  public static final String OPENAPI_OPERATIONID = "operationId";
  public static final String REST_OPENAPI_COMPONENT = "rest-openapi://";

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(RouteContextService.class);

  @Autowired
  CamelContext camelContext;

  @Autowired
  Environment env;

  private List<CamelRoute> routes;

  /**
   * Returns CamelRoutes.
   *
   * @return route list with the links.
   */
  public List<CamelRoute> getCamelRoutes() {

    if (routes != null) {
      return routes;
    }
    routes = new ArrayList<>();

    List<CamelRoute> restRoutes = new ArrayList<>();

    for (Route route : camelContext.getRoutes()) {
      String routeId = route.getId();

      RouteDefinition routeDefinition = ((ModelCamelContext) camelContext)
          .getRouteDefinition(routeId);

      List<CamelRouteOutput> outputs = new ArrayList<>();

      extractOutputs(routeDefinition.getOutputs(), outputs);

      boolean isRestApiRoute = checkRestOpenApiRouteDefinition(routeDefinition, outputs);

      String errorHandler = null;

      if (routeDefinition.getErrorHandlerFactory() instanceof DeadLetterChannelBuilder deadLetterChannelBuilder) {
        errorHandler = deadLetterChannelBuilder.getDeadLetterUri();
      }

      CamelRoute metaRoute = new CamelRoute(routeDefinition.getId(),
          updateWithSystemProperties(routeDefinition.getInput().toString()), outputs,
          routeDefinition.isRest(), errorHandler);

      if (isRestApiRoute) {
        restRoutes.add(metaRoute);
      } else {
        routes.add(metaRoute);
      }

    }
    /*
     set the rest property to true of the routes that are called
     directly from the rest-openapi route,
     this is not done by camel anymore if you use rest-openapi with a yaml file.
     */
    adjustRestInputRoutes(restRoutes, routes);

    return routes;
  }

  private void extractOutputs(List<ProcessorDefinition<?>> outputss,
      List<CamelRouteOutput> outputs) {

    ProcessorDefinitionHelper.filterTypeInOutputs(outputss, ToDefinition.class).stream()
        .forEach(p -> outputs.add(new CamelRouteOutput(p.getId(), updateWithSystemProperties(p.toString()),
            null, p.getClass().getTypeName(), null)));

    ProcessorDefinitionHelper.filterTypeInOutputs(outputss, ToDynamicDefinition.class).stream()
        .forEach(p -> outputs.add(new CamelRouteOutput(p.getId(), updateWithSystemProperties(p.toString()),
            null, p.getClass().getTypeName(), null)));

    ProcessorDefinitionHelper.filterTypeInOutputs(outputss, EnrichDefinition.class).stream()
        .forEach(p -> outputs.add(new CamelRouteOutput(p.getId(), updateWithSystemProperties(p.toString()),
            null, p.getClass().getTypeName(), null)));

    ProcessorDefinitionHelper.filterTypeInOutputs(outputss, PollEnrichDefinition.class).stream()
        .forEach(p -> outputs.add(new CamelRouteOutput(p.getId(), updateWithSystemProperties(p.toString()),
            null, p.getClass().getTypeName(), null)));

    ProcessorDefinitionHelper.filterTypeInOutputs(outputss, RecipientListDefinition.class).stream()
        .forEach(p -> outputs.add(new CamelRouteOutput(p.getId(), updateWithSystemProperties(p.toString()),
            p.getDelimiter(), p.getClass().getTypeName(), null)));

    ProcessorDefinitionHelper.filterTypeInOutputs(outputss, RoutingSlipDefinition.class).stream()
        .forEach(p -> outputs.add(new CamelRouteOutput(p.getId(), updateWithSystemProperties(p.toString()),
            p.getUriDelimiter(), p.getClass().getTypeName(), null)));

  }

  private String updateWithSystemProperties(String id) {

    if (id.contains("{{") && id.contains("}}")) {
      Pattern pattern = Pattern.compile("\\{\\{(.*?)}}");
      Matcher matcher = pattern.matcher(id);

      // Replace the matched values with their corresponding environment values
      StringBuffer result = new StringBuffer();
      while (matcher.find()) {
        String key = matcher.group(1);
        String replacement = env.getProperty(key);
        matcher.appendReplacement(result, replacement);
      }
      matcher.appendTail(result);

      return result.toString();

    } else {
      return id;
    }

  }

  private void adjustRestInputRoutes(List<CamelRoute> restApiRoutes, List<CamelRoute> routes) {

    restApiRoutes.stream().forEach(e -> e.getOutputs().forEach(p -> routes.stream().filter(r -> r.getInput().equals(p.getDescription())).forEach(s -> s.setRest(
        true))
    ));
  }

  private boolean checkRestOpenApiRouteDefinition(RouteDefinition routeDefinition, List<CamelRouteOutput> outputs) {
    String inputUri = routeDefinition.getInput() != null ? routeDefinition.getInput().getUri() : null;

    if (inputUri != null && inputUri.contains(REST_OPENAPI_COMPONENT)) {

      int startIndex = inputUri.indexOf(REST_OPENAPI_COMPONENT) + REST_OPENAPI_COMPONENT.length();

      int endIndex = inputUri.indexOf("?", startIndex);

      String openApiPath = inputUri.substring(startIndex, endIndex);
      List<String> operationIds = null;

      if (openApiPath.endsWith(".json")) {
        operationIds = readOperationIdsFromJson(openApiPath);
      } else if (openApiPath.endsWith(".yml") || openApiPath.endsWith(".yaml")) {
        operationIds = readOperationIdsFromYaml(openApiPath);
      } else {
        LOGGER.warn("Unknown file type for the OpenAPI spec: {}", openApiPath);
        return false;
      }

      operationIds.forEach(p -> outputs.add(new CamelRouteOutput("", "From[direct:" + p + "]", null, null, null)));

      return true;
    }

    return false;

  }

  private List<String> readOperationIdsFromYaml(String openApiPath) {

    List<String> operationIds = new ArrayList<>();

    Yaml yaml = new Yaml();

    try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(openApiPath)) {

      Map<String, Object> data = yaml.load(inputStream);

      Map<String, Map<String, Map<String, Object>>> paths = (Map<String, Map<String, Map<String, Object>>>) data.get("paths");

      for (Map<String, Map<String, Object>> methods : paths.values()) {
        for (Map<String, Object> methodData : methods.values()) {
          if (methodData.containsKey(OPENAPI_OPERATIONID)) {
            operationIds.add(methodData.get(OPENAPI_OPERATIONID).toString());
          }
        }
      }

    } catch (Exception e) {
      LOGGER.warn("Could not read the OpenApi spec: {} with exception: {}", openApiPath, e);
    }

    return operationIds;
  }

  private List<String> readOperationIdsFromJson(String openApiPath) {

    List<String> operationIds = new ArrayList<>();

    ObjectMapper mapper = new ObjectMapper();

    try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(openApiPath)) {

      JsonNode rootNode = mapper.readTree(inputStream);

      JsonNode pathsNode = rootNode.get("paths");
      if (pathsNode != null) {
        pathsNode.fields().forEachRemaining(entry -> {
          JsonNode methodsNode = entry.getValue();
          methodsNode.fields().forEachRemaining(method -> {
            JsonNode operationIdNode = method.getValue().get(OPENAPI_OPERATIONID);
            if (operationIdNode != null) {
              operationIds.add(operationIdNode.asText());
            }
          });
        });
      }

    } catch (IOException e) {
      LOGGER.warn("Could not read the OpenApi spec: {} with exception: {}", openApiPath, e);
    }

    return operationIds;
  }

}
