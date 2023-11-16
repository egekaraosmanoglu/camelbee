package org.camelbee.debugger.controller;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.camelbee.tracers.TracerService;

@Path("/")
@IfBuildProperty(name = "camelbee.context-enabled", stringValue = "true")
@IfBuildProperty(name = "camelbee.debugger-enabled", stringValue = "true")
public class TracerController {

    @Inject
    TracerService tracerService;

    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/camelbee/tracer/keepActive")
    public Response getKeepActive() {

        tracerService.keepTracingActive();
        return Response.ok("keeping active!").build();
    }

}
