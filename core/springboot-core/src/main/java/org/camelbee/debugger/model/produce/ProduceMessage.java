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

package org.camelbee.debugger.model.produce;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ProduceMessage.
 */
public class ProduceMessage {

  private final String routeName;

  private final String message;

  private final String clazz;

  private final String mediaType;

  private final ProduceMessageHeaderList headers;

  private final Boolean traceEnabled;

  /**
   * Constructor.
   *
   * @param routeName    The routeName.
   * @param message      The message.
   * @param clazz        The message.
   * @param mediaType    The message.
   * @param headers      The message.
   * @param traceEnabled The message.
   */
  @JsonCreator
  public ProduceMessage(
      @JsonProperty("routeName") String routeName,
      @JsonProperty("message") String message,
      @JsonProperty("clazz") String clazz,
      @JsonProperty("mediaType") String mediaType,
      @JsonProperty("headers") ProduceMessageHeaderList headers,
      @JsonProperty("traceEnabled") Boolean traceEnabled) {
    this.routeName = routeName;
    this.message = message;
    this.clazz = clazz;
    this.mediaType = mediaType;
    this.headers = headers;
    this.traceEnabled = traceEnabled;
  }

  public String getRouteName() {
    return routeName;
  }

  public String getMessage() {
    return message;
  }

  public String getClazz() {
    return clazz;
  }

  public String getMediaType() {
    return mediaType;
  }

  public ProduceMessageHeaderList getHeaders() {
    return headers;
  }

  public Boolean getTraceEnabled() {
    return traceEnabled;
  }
}
