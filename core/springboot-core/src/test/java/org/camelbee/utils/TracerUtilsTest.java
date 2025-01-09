package org.camelbee.utils;

import static org.camelbee.constants.CamelBeeConstants.CAMEL_FAILED_EVENT_IDENTITIY_HASHCODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TracerUtilsTest {

  @Mock
  private Exchange exchange;

  @Mock
  private Message message;

  @Test
  void getHeadersShouldReturnFormattedHeaders() {
    // Arrange
    Map<String, Object> headers = new HashMap<>();
    headers.put("header1", "value1");
    headers.put("header2", "value2");

    when(exchange.getIn()).thenReturn(message);
    when(message.getHeaders()).thenReturn(headers);

    // Act
    String result = TracerUtils.getHeaders(exchange);

    // Assert
    assertTrue(result.contains("header1:value1"));
    assertTrue(result.contains("header2:value2"));
    assertTrue(result.contains("\n"));
  }

  @Test
  void getHeadersShouldReturnEmptyStringForNoHeaders() {
    // Arrange
    Map<String, Object> headers = new HashMap<>();

    when(exchange.getIn()).thenReturn(message);
    when(message.getHeaders()).thenReturn(headers);

    // Act
    String result = TracerUtils.getHeaders(exchange);

    // Assert
    assertEquals("", result);
  }

  @Test
  void handleErrorShouldReturnNullWhenNoException() {
    // Arrange
    when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)).thenReturn(null);
    when(exchange.getException()).thenReturn(null);

    // Act
    String result = TracerUtils.handleError(exchange);

    // Assert
    assertNull(result);
  }

  @Test
  void handleErrorShouldReturnMessageForNewException() {
    // Arrange
    Exception testException = new RuntimeException("Test error message");

    when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)).thenReturn(testException);
    when(exchange.getProperty(CAMEL_FAILED_EVENT_IDENTITIY_HASHCODE)).thenReturn(null);

    // Act
    String result = TracerUtils.handleError(exchange);

    // Assert
    assertEquals("Test error message", result);
    verify(exchange).setProperty(eq(CAMEL_FAILED_EVENT_IDENTITIY_HASHCODE), anyInt());
  }

  @Test
  void handleErrorShouldReturnNullForPreviouslyTracedException() {
    // Arrange
    Exception testException = new RuntimeException("Test error message");
    int exceptionHashCode = System.identityHashCode(testException);

    when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)).thenReturn(testException);
    when(exchange.getProperty(CAMEL_FAILED_EVENT_IDENTITIY_HASHCODE)).thenReturn(exceptionHashCode);

    // Act
    String result = TracerUtils.handleError(exchange);

    // Assert
    assertNull(result);
  }

  @Test
  void handleErrorShouldCheckExchangeExceptionWhenExceptionCaughtIsNull() {
    // Arrange
    Exception testException = new RuntimeException("Test error message");

    when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)).thenReturn(null);
    when(exchange.getException()).thenReturn(testException);
    when(exchange.getProperty(CAMEL_FAILED_EVENT_IDENTITIY_HASHCODE)).thenReturn(null);

    // Act
    String result = TracerUtils.handleError(exchange);

    // Assert
    assertEquals("Test error message", result);
    verify(exchange).setProperty(eq(CAMEL_FAILED_EVENT_IDENTITIY_HASHCODE), anyInt());
  }
}
