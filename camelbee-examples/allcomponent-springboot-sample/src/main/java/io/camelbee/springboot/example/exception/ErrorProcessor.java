package io.camelbee.springboot.example.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Error processor.
 *
 * @author ekaraosmanoglu
 */

@Component
public class ErrorProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {

    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");

    exchange.getIn().setBody(cause.getLocalizedMessage());


  }
}
