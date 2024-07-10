package org.camelbee.debugger.service;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * RouteContextService.
 */
@Component
public class RouteContextService {

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

    for (Route route : camelContext.getRoutes()) {
      String routeId = route.getId();

      RouteDefinition routeDefinition = ((ModelCamelContext) camelContext)
          .getRouteDefinition(routeId);

      List<CamelRouteOutput> outputs = new ArrayList<>();

      extractOutputs(routeDefinition.getOutputs(), outputs);

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
}
