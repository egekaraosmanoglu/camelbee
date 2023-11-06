package org.camelbee.debugger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.commons.lang3.StringUtils;
import org.camelbee.debugger.model.produce.ProduceMessage;
import org.eclipse.microprofile.config.Config;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Path("/")
public class ProducerController {

    @Inject
    CamelContext camelContext;

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Config config;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/camelbee/produce/direct")
    public Response produceDirect(@Valid ProduceMessage produceMessage)
            throws Exception {
        Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();

        Map<String, Object> defaultHeaders = produceMessage.getHeaders().getHeaders().stream()
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        exchange.getIn().setHeaders(defaultHeaders);

        final Object request;

        String response = "";

        try {

            if (produceMessage.getMediaType() != null && produceMessage.getMediaType().equals("json")
                    && !StringUtils.isEmpty(produceMessage.getClazz())) {
                request = objectMapper.readValue(produceMessage.getMessage(), Class.forName(produceMessage.getClazz()));

            } else if (produceMessage.getMediaType() != null && produceMessage.getMediaType().equals("xml")) {

                request = produceMessage.getMessage();

            } else {
                request = produceMessage.getMessage();
            }

            exchange.getIn().setBody(request);

            Exchange result = producerTemplate.send(adjustRouteName(produceMessage.getRouteName()), exchange);
            response = result.getMessage().getBody(String.class);

        } catch (Exception e) {
            response = e.getLocalizedMessage();
        }

        return Response.ok(response).build();

    }

    public String adjustRouteName(String routeName) {

        String adjustedRouteName = null;

        if (routeName.startsWith("From[rest:")) {
            adjustedRouteName = "http:localhost:{{local.server.port}}/camel" + applyPattern(routeName, "://[a-zA-Z]+:(/[^?]+)")
                    + "?throwExceptionOnFailure=false";
        } else if (routeName.startsWith("From[jpa:")) {
            adjustedRouteName = applyPattern(routeName, "From\\[(jpa:[^?]+)");
        } else {
            adjustedRouteName = applyPattern(routeName, "\\[([^\\]]+)\\]");
        }

        return adjustedRouteName;
    }

    public static String applyPattern(String input, String patternVal) {

        Pattern pattern = java.util.regex.Pattern.compile(patternVal);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
