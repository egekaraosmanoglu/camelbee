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

import org.camelbee.tracers.TracerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:8083")
@ConditionalOnExpression("'${camelbee.context-enabled:false}' && '${camelbee.debugger-enabled:false}'")
public class TracerController {

    @Autowired
    TracerService tracerService;

    @GetMapping(value = "/camelbee/tracer/keepActive")
    public ResponseEntity<String> getMessages() {
        tracerService.keepTracingActive();
        return ResponseEntity.ok("keeping active!");
    }

}
