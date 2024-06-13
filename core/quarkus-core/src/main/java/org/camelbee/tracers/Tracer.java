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

package org.camelbee.tracers;

import java.io.IOException;
import java.io.InputStream;
import org.apache.camel.Exchange;
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.cxf.message.MessageContentsList;

abstract class Tracer {

  private TracerVisitor tracerVisitor;

  public void accept(TracerVisitor tracerVisitor) {
    this.tracerVisitor = tracerVisitor;
  }

  public void visit(Exchange exchange) {
    if (tracerVisitor != null) {
      tracerVisitor.visit(exchange);
    }
  }

  protected String getBodyAndConvertInputStreamsToString(Exchange exchange) throws IOException {

    String response = null;

    if (exchange.getIn().getBody() instanceof MessageContentsList msgList) {

      response = !msgList.isEmpty() ? msgList.get(0).toString() : null;

    } else if (exchange.getIn().getBody() instanceof InputStreamCache requestStreamCache) {

      response = new String(requestStreamCache.readAllBytes());

      exchange.getIn().setBody(response);

    } else if (exchange.getIn().getBody() instanceof InputStream requestStream) {

      response = new String(requestStream.readAllBytes());

      exchange.getIn().setBody(response);

    } else if (exchange.getIn().getBody() != null) {
      response = exchange.getIn().getBody(String.class);
    }

    return response;
  }
}
