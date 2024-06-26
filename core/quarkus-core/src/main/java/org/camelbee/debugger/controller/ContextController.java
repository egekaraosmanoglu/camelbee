/*
 * Copyright 2023 Rahmi Ege Karaosmanoglu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camelbee.debugger.controller;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.MessageList;
import org.camelbee.debugger.model.route.CamelBeeContext;
import org.camelbee.debugger.model.route.CamelRoute;
import org.camelbee.debugger.model.route.CamelRouteOutput;
import org.camelbee.debugger.service.MessageService;
import org.eclipse.microprofile.config.Config;

/**
 * ContextController exposes routes topology and messages.
 */
@Path("/")
@IfBuildProperty(name = "camelbee.context-enabled", stringValue = "true")
public class ContextController {

  @Inject
  CamelContext camelContext;

  @Inject
  MessageService messageService;

  @Inject
  Config config;

  /**
   * Returns the Routes list of the camelContext and their outputs.
   *
   * @return CamelBeeContext The routes topology.
   */
  @GET
  @Consumes("application/json")
  @Produces("application/json")
  @Path("/camelbee/routes")
  public Response getWidgets() {

    List<CamelRoute> routes = getCamelRoutes();

    String name = camelContext.getName();

    String jvm = "%s - %s".formatted(System.getProperty(CamelBeeConstants.SYSTEM_JVM_VENDOR),
        System.getProperty(CamelBeeConstants.SYSTEM_JVM_VERSION));

    String framework = "%s - %s".formatted(CamelBeeConstants.FRAMEWORK,
        io.quarkus.runtime.Quarkus.class.getPackage().getImplementationVersion());

    String camelVersion = camelContext.getVersion();

    String jvmInputParameters = ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
        .collect(Collectors.joining(", "));

    String garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans().stream().map(GarbageCollectorMXBean::getName)
        .collect(Collectors.joining(", "));

    return Response
        .ok(new CamelBeeContext(routes, name, jvm, jvmInputParameters, garbageCollectors, framework, camelVersion))
        .build();
  }

  private List<CamelRoute> getCamelRoutes() {
    List<CamelRoute> routes = new ArrayList<>();

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

  @GET
  @Consumes("application/json")
  @Produces("application/json")
  @Path("/camelbee/messages")
  public Response getMessages() {
    return Response.ok(new MessageList(messageService.getMessageList())).build();

  }

  /**
   * Delete messages.
   *
   * @return String The success message.
   */
  @DELETE
  @Consumes("application/json")
  @Produces("application/json")
  @Path("/camelbee/messages")
  public Response deleteMessages() {

    messageService.reset();

    return Response.ok("deleted.").build();

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

}
