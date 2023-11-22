package org.camelbee.debugger.controller;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.camelbee.tracers.TracerService;

@Path("/")
@IfBuildProperty(name = "camelbee.context-enabled", stringValue = "true")
@IfBuildProperty(name = "camelbee.debugger-enabled", stringValue = "true")
public class TracerController {

    private enum TraceStatus {
        ACTIVE,
        DEACTIVE
    }

    @Inject
    TracerService tracerService;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/camelbee/tracer/status")
    public Response updateTraceStatus(@Valid TraceStatus traceStatus) {

        if (traceStatus == TraceStatus.ACTIVE) {
            tracerService.setTracingEnabled(true);
            tracerService.keepTracingActive();
        } else if (traceStatus == TraceStatus.DEACTIVE) {
            tracerService.setTracingEnabled(false);
        }

        return Response.ok("tracing status updated as:" + traceStatus.toString()).build();
    }

}
