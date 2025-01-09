package org.camelbee.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.StreamCache;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExchangeUtilsTest {

  @Mock
  private Exchange exchange;

  @Mock
  private Message message;

  @Mock
  private StreamCache streamCache;

  @BeforeEach
  void setUp() {
    when(exchange.getIn()).thenReturn(message);
  }

  @Test
  void getHeadersShouldReturnFormattedHeaders() {
    // Arrange
    Map<String, Object> headers = new HashMap<>();
    headers.put("header1", "value1");
    headers.put("header2", "value2");

    when(message.getHeaders()).thenReturn(headers);

    // Act
    String result = ExchangeUtils.getHeaders(exchange);

    // Assert
    assertTrue(result.contains("header1:value1"));
    assertTrue(result.contains("header2:value2"));
    assertTrue(result.contains("\n"));
  }

  @Test
  void getHeadersShouldReturnEmptyStringForNoHeaders() {
    // Arrange
    when(message.getHeaders()).thenReturn(new HashMap<>());

    // Act
    String result = ExchangeUtils.getHeaders(exchange);

    // Assert
    assertEquals("", result);
  }

  @Test
  void readBodyAsStringShouldHandleStreamCache() throws IOException {
    // Arrange
    String expectedContent = "Test content";
    when(message.getBody()).thenReturn(streamCache);
    doAnswer(invocation -> {
      ByteArrayOutputStream baos = (ByteArrayOutputStream) invocation.getArgument(0);
      baos.write(expectedContent.getBytes());
      return null;
    }).when(streamCache).writeTo(any(ByteArrayOutputStream.class));

    // Act
    String result = ExchangeUtils.readBodyAsString(exchange, true);

    // Assert
    assertEquals(expectedContent, result);
    verify(streamCache, times(2)).reset();
    verify(streamCache).writeTo(any(ByteArrayOutputStream.class));
  }

  @Test
  void readBodyAsStringShouldHandleArrayList() throws IOException {
    // Arrange
    ArrayList<String> list = new ArrayList<>();
    list.add("item1");
    list.add("item2");
    when(message.getBody()).thenReturn(list);

    // Act
    String result = ExchangeUtils.readBodyAsString(exchange, false);

    // Assert
    assertEquals(list.toString(), result);
  }

  @Test
  void readBodyAsStringShouldHandleStringBody() throws IOException {
    // Arrange
    String expectedContent = "Test string content";
    when(message.getBody()).thenReturn(expectedContent);
    when(message.getBody(String.class)).thenReturn(expectedContent);

    // Act
    String result = ExchangeUtils.readBodyAsString(exchange, false);

    // Assert
    assertEquals(expectedContent, result);
  }

  @Test
  void readBodyAsStringShouldReturnNullForNullBody() throws IOException {
    // Arrange
    when(message.getBody()).thenReturn(null);

    // Act
    String result = ExchangeUtils.readBodyAsString(exchange, false);

    // Assert
    assertNull(result);
  }

  @Test
  void readBodyAsStringShouldHandleException() throws IOException {
    // Arrange
    when(message.getBody()).thenThrow(new RuntimeException("Test exception"));

    // Act
    String result = ExchangeUtils.readBodyAsString(exchange, false);

    // Assert
    assertEquals(StringUtils.EMPTY, result);
  }

  @Test
  void readBodyAsStringShouldHandleStreamCacheWithoutReset() throws IOException {
    // Arrange
    String expectedContent = "Test content";
    when(message.getBody()).thenReturn(streamCache);
    doAnswer(invocation -> {
      ByteArrayOutputStream baos = (ByteArrayOutputStream) invocation.getArgument(0);
      baos.write(expectedContent.getBytes());
      return null;
    }).when(streamCache).writeTo(any(ByteArrayOutputStream.class));

    // Act
    String result = ExchangeUtils.readBodyAsString(exchange, false);

    // Assert
    assertEquals(expectedContent, result);
    verify(streamCache, times(1)).reset();
    verify(streamCache).writeTo(any(ByteArrayOutputStream.class));
  }

  @Test
  void readBodyAsStringShouldHandleStreamCacheIOException() throws IOException {
    // Arrange
    when(message.getBody()).thenReturn(streamCache);
    doThrow(new IOException("Test IO exception")).when(streamCache).writeTo(any(ByteArrayOutputStream.class));

    // Act
    String result = ExchangeUtils.readBodyAsString(exchange, true);

    // Assert
    assertEquals(StringUtils.EMPTY, result);
    verify(streamCache).reset();
  }
}
