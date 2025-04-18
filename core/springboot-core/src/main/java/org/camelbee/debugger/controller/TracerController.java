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

import jakarta.validation.Valid;
import org.camelbee.tracers.TracerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * TracerController.
 */
@RestController
@CrossOrigin(origins = {"https://www.camelbee.io", "http://localhost:8083"})
@ConditionalOnExpression("'${camelbee.context-enabled:false}' && '${camelbee.tracer-enabled:false}'")
public class TracerController {

  private enum TraceStatus {
    ACTIVE, DEACTIVE
  }

  @Autowired
  TracerService tracerService;

  /**
   * Enables/Disables tracing.
   *
   * @param traceStatus The traceStatus.
   * @return String The result.
   */
  @PostMapping(value = "/camelbee/tracer/status", produces = "application/json", consumes = "application/json")
  public ResponseEntity<String> updateTraceStatus(@Valid @RequestBody(required = true) TraceStatus traceStatus) {

    if (traceStatus == TraceStatus.ACTIVE) {
      tracerService.activateTracing(true);
      tracerService.keepTracingActive();
    } else if (traceStatus == TraceStatus.DEACTIVE) {
      tracerService.activateTracing(false);
    }

    return ResponseEntity.ok("tracing status updated as:" + traceStatus.toString());
  }

}
