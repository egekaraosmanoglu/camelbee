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

package org.camelbee.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.camel.Exchange;
import org.apache.camel.StreamCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExchangeUtils.
 */
public class ExchangeUtils {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeUtils.class);

  private ExchangeUtils() {
    // Private constructor
  }

  /**
   * Return all the headers concatenated.
   *
   * @param exchange The exchange.
   * @return String The headers.
   */
  public static String getHeaders(Exchange exchange) {

    var headers = new StringBuilder();

    exchange.getIn().getHeaders()
        .forEach((p, q) -> headers.append(p).append(":").append(q).append("\n"));

    return headers.toString();
  }

  /**
   * Reads all kind of bodies and convert to string.
   *
   * @param exchange The Exchange.
   * @return String body.
   * @throws IOException The exception.
   */
  @SuppressWarnings("java:S3740")
  public static String readBodyAsString(Exchange exchange, boolean resetBefore) {
    try {
      // Determine whether to use getMessage() or getIn()
      boolean useMessage = exchange.getMessage() != null && exchange.getMessage().getBody() != null;

      // Extract body object using the determined approach
      Object bodyObject = useMessage
          ? exchange.getMessage().getBody()
          : exchange.getIn().getBody();

      // Return null if body is null
      if (bodyObject == null) {
        return null;
      }

      // Handle different body types
      if (bodyObject instanceof StreamCache streamCache) {
        return processStreamCache(streamCache, resetBefore);
      } else if (bodyObject instanceof ArrayList) {
        // Specifically for cxf MessageContentsList
        return bodyObject.toString();
      } else {
        // Use the same message source as we determined earlier
        return useMessage
            ? exchange.getMessage().getBody(String.class)
            : exchange.getIn().getBody(String.class);
      }
    } catch (Exception e) {
      LOGGER.warn("Could not read Exchange body: {} with exception: {}", exchange, e);
      return StringUtils.EMPTY;
    }
  }

  private static String processStreamCache(StreamCache streamCache, boolean resetBefore) throws IOException {
    if (resetBefore) {
      streamCache.reset();
    }

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    streamCache.writeTo(byteArrayOutputStream);
    String body = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    streamCache.reset();

    return body;
  }

}
