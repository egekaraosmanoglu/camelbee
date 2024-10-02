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
  public static String readBodyAsString(Exchange exchange, boolean resetBefore) throws IOException {

    try {

      String response = null;

      if (exchange.getIn().getBody() instanceof StreamCache streamCache) {

        if (resetBefore) {
          streamCache.reset();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        streamCache.writeTo(byteArrayOutputStream);

        response = byteArrayOutputStream.toString("UTF-8");

        streamCache.reset();

      } else if (exchange.getIn().getBody() instanceof ArrayList arrayList) {
        //espacially for cxf MessageContentsList
        response = arrayList.toString();
      } else if (exchange.getIn().getBody() != null) {
        response = exchange.getIn().getBody(String.class);
      }

      return response;

    } catch (Exception e) {
      LOGGER.warn("Could not Exchange body: {} with exception: {}", exchange, e);
      return StringUtils.EMPTY;
    }
  }

}
