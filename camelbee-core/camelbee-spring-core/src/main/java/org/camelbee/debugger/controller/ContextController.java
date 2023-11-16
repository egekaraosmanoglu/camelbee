/*
 * Copyright 2023 Rahmi Ege Karaosmanoglu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camelbee.debugger.controller;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.model.*;
import org.camelbee.debugger.model.exchange.MessageList;
import org.camelbee.debugger.model.route.CamelRoute;
import org.camelbee.debugger.model.route.CamelRouteList;
import org.camelbee.debugger.model.route.CamelRouteOutput;
import org.camelbee.debugger.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@CrossOrigin(origins = "https://www.camelbee.io")
@ConditionalOnProperty(value = "camelbee.context-enabled", havingValue = "true")
public class ContextController {

    @Autowired
    CamelContext camelContext;

    @Autowired
    MessageService messageService;

    @Autowired
    Environment env;

    @GetMapping(value = "/camelbee/routes")
    public ResponseEntity<CamelRouteList> getRoutes() {

        List<CamelRoute> routes = getCamelRoutes();

        String name = camelContext.getName();

        return ResponseEntity.ok(new CamelRouteList(routes, name));
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

    @GetMapping(value = "/camelbee/messages")
    public ResponseEntity<MessageList> getMessages() {
        return ResponseEntity.ok(new MessageList(messageService.getMessageList()));
    }

    @DeleteMapping(value = "/camelbee/messages")
    public ResponseEntity<String> deleteMessages() {

        messageService.reset();

        return ResponseEntity.ok("deleted.");

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
