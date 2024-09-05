package org.camelbee.debugger.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.eclipse.microprofile.config.Config;

/**
 * RouteContextService.
 */
@ApplicationScoped
public class RouteContextService {

  @Inject
  CamelContext camelContext;

  @Inject
  Config config;

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

    for (Route route : camelContext.getRoutes()) {
      String routeId = route.getId();

      RouteDefinition routeDefinition = ((ModelCamelContext) camelContext)
          .getRouteDefinition(routeId);

      List<CamelRouteOutput> outputs = new ArrayList<>();

      extractOutputs(routeDefinition.getOutputs(), outputs);

      checkRestOpenApiRouteDefinition(routeDefinition, outputs);

      String errorHandler = null;

      if (routeDefinition.getErrorHandlerFactory() instanceof DeadLetterChannelBuilder deadLetterChannelBuilder) {
        errorHandler = deadLetterChannelBuilder.getDeadLetterUri();
      }

      CamelRoute metaRoute = new CamelRoute(routeDefinition.getId(),
          updateWithSystemProperties(routeDefinition.getInput().toString()), outputs,
          routeDefinition.isRest(), errorHandler);

      routes.add(metaRoute);
    }
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

      // Replace the matched values with their corresponding hard-coded values
      StringBuffer result = new StringBuffer();
      while (matcher.find()) {
        String key = matcher.group(1);
        Optional<String> replacement = config.getOptionalValue(key, String.class);
        matcher.appendReplacement(result, replacement.orElse(""));
      }
      matcher.appendTail(result);

      return result.toString();

    } else {
      return id;
    }

  }

  private void checkRestOpenApiRouteDefinition(RouteDefinition routeDefinition, List<CamelRouteOutput> outputs) {
    String inputUri = routeDefinition.getInput() != null ? routeDefinition.getInput().getUri() : null;

    if (inputUri != null && inputUri.contains("rest-openapi://")) {

      // Find the start index after "rest-openapi://"
      int startIndex = inputUri.indexOf("rest-openapi://") + "rest-openapi://".length();

      // Find the end index before the first "?" character
      int endIndex = inputUri.indexOf("?", startIndex);

      // Extract the substring
      String openApiPath = inputUri.substring(startIndex, endIndex);

      InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(openApiPath);

      if (inputStream == null) {
        System.out.println("File not found in resources");
        return;
      }

      try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
        String line;
        while ((line = br.readLine()) != null) {
          if (line.trim().startsWith("operationId:")) {
            // Extracting the part after "operationId:"
            String operationId = line.substring(line.indexOf("operationId:") + "operationId:".length()).trim();
            System.out.println(operationId);

            outputs.add(new CamelRouteOutput("", "To[direct:" + operationId + "]",
                null, null, null));
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    //"rest-openapi://openapi/myvfz.yaml?missingOperation=ignore&produces=application%2Fjson&consumes=application%2Fjson"
  }

}
