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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.camel.CamelContext;
import org.camelbee.constants.CamelBeeConstants;
import org.camelbee.debugger.model.exchange.Message;
import org.camelbee.debugger.model.exchange.MessageEventType;
import org.camelbee.debugger.model.exchange.MessageList;
import org.camelbee.debugger.model.exchange.MessageType;
import org.camelbee.debugger.model.route.CamelBeeContext;
import org.camelbee.debugger.model.route.CamelRoute;
import org.camelbee.debugger.model.route.CamelRouteOutput;
import org.camelbee.debugger.service.MessageService;
import org.camelbee.debugger.service.RouteContextService;
import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContextControllerTest {

  @Mock
  private CamelContext camelContext;

  @Mock
  private MessageService messageService;

  @Mock
  private RouteContextService routeContextService;

  @Mock
  private Config config;

  @InjectMocks
  private ContextController contextController;

  private static final String TEST_ROUTE_ID_1 = "route1";
  private static final String TEST_ROUTE_ID_2 = "route2";

  @BeforeEach
  void setUp() {
    System.setProperty(CamelBeeConstants.SYSTEM_JVM_VENDOR, "Test Vendor");
    System.setProperty(CamelBeeConstants.SYSTEM_JVM_VERSION, "11.0.1");
  }

  @Test
  void getWidgetsShouldReturnRoutesAndSystemInfo() {
    // Arrange
    List<CamelRouteOutput> nestedOutputs = Arrays.asList(
        new CamelRouteOutput("nested1", "Nested Output 1", ",", "log", null),
        new CamelRouteOutput("nested2", "Nested Output 2", ";", "mock", null)
    );

    List<CamelRouteOutput> outputs1 = Arrays.asList(
        new CamelRouteOutput("output1", "First Output", "|", "direct", nestedOutputs),
        new CamelRouteOutput("output2", "Second Output", ",", "seda", null)
    );
    List<CamelRouteOutput> outputs2 = Arrays.asList(
        new CamelRouteOutput("output3", "Third Output", "-", "vm", null)
    );

    List<CamelRoute> mockRoutes = Arrays.asList(
        new CamelRoute(TEST_ROUTE_ID_1, "direct:start1", outputs1, false, "direct:error1"),
        new CamelRoute(TEST_ROUTE_ID_2, "direct:start2", outputs2, true, "direct:error2")
    );

    when(routeContextService.getCamelRoutes()).thenReturn(mockRoutes);
    when(camelContext.getName()).thenReturn("TestContext");
    when(camelContext.getVersion()).thenReturn("3.18.0");

    // Act
    Response response = contextController.getWidgets();

    // Assert
    assertEquals(200, response.getStatus());
    CamelBeeContext context = (CamelBeeContext) response.getEntity();
    assertNotNull(context);
    assertEquals("TestContext", context.getName());
    assertEquals(mockRoutes, context.getRoutes());
    assertEquals("Test Vendor - 11.0.1", context.getJvm());
    assertEquals("3.18.0", context.getCamelVersion());
    assertTrue(context.getFramework().startsWith(CamelBeeConstants.FRAMEWORK));
    assertNotNull(context.getJvmInputParameters());
    assertNotNull(context.getGarbageCollectors());

    List<CamelRoute> routes = context.getRoutes();
    assertEquals(2, routes.size());

    CamelRoute route1 = routes.get(0);
    assertEquals(TEST_ROUTE_ID_1, route1.getId());
    assertEquals("direct:start1", route1.getInput());
    assertEquals(2, route1.getOutputs().size());
    assertFalse(route1.getRest());
    assertEquals("direct:error1", route1.getErrorHandler());

    // Verify first route outputs in detail
    CamelRouteOutput firstOutput = route1.getOutputs().get(0);
    assertEquals("output1", firstOutput.getId());
    assertEquals("First Output", firstOutput.getDescription());
    assertEquals("|", firstOutput.getDelimiter());
    assertEquals("direct", firstOutput.getType());
    assertNotNull(firstOutput.getOutputs());
    assertEquals(2, firstOutput.getOutputs().size());

    // Verify nested outputs
    CamelRouteOutput nestedOutput = firstOutput.getOutputs().get(0);
    assertEquals("nested1", nestedOutput.getId());
    assertEquals("Nested Output 1", nestedOutput.getDescription());
    assertEquals(",", nestedOutput.getDelimiter());
    assertEquals("log", nestedOutput.getType());
    assertNull(nestedOutput.getOutputs());

    CamelRoute route2 = routes.get(1);
    assertEquals(TEST_ROUTE_ID_2, route2.getId());
    assertEquals("direct:start2", route2.getInput());
    assertEquals(1, route2.getOutputs().size());
    assertTrue(route2.getRest());
    assertEquals("direct:error2", route2.getErrorHandler());

    // Verify second route output
    CamelRouteOutput route2Output = route2.getOutputs().get(0);
    assertEquals("output3", route2Output.getId());
    assertEquals("Third Output", route2Output.getDescription());
    assertEquals("-", route2Output.getDelimiter());
    assertEquals("vm", route2Output.getType());
    assertNull(route2Output.getOutputs());
  }

  @Test
  void getMessagesShouldReturnMessageListWithDifferentEventTypes() {
    // Arrange
    List<Message> mockMessages = Arrays.asList(
        new Message("id1", MessageEventType.CREATED, "body1", "headers1", TEST_ROUTE_ID_1, "endpoint1", "endpointId1", MessageType.REQUEST, null),
        new Message("id2", MessageEventType.SENDING, "body2", "headers2", TEST_ROUTE_ID_1, "endpoint2", "endpointId2", MessageType.RESPONSE, null),
        new Message("id3", MessageEventType.SENT, "body3", "headers3", TEST_ROUTE_ID_2, "endpoint3", "endpointId3", MessageType.REQUEST, null),
        new Message("id4", MessageEventType.COMPLETED, "body4", "headers4", TEST_ROUTE_ID_2, "endpoint4", "endpointId4", MessageType.RESPONSE, null)
    );
    when(messageService.getMessageList()).thenReturn(mockMessages);

    // Act
    Response response = contextController.getMessages();

    // Assert
    assertEquals(200, response.getStatus());
    MessageList messageList = (MessageList) response.getEntity();
    assertNotNull(messageList);
    assertEquals(4, messageList.getMessages().size());

    // Verify different message event types
    assertEquals(MessageEventType.CREATED, messageList.getMessages().get(0).getExchangeEventType());
    assertEquals(MessageEventType.SENDING, messageList.getMessages().get(1).getExchangeEventType());
    assertEquals(MessageEventType.SENT, messageList.getMessages().get(2).getExchangeEventType());
    assertEquals(MessageEventType.COMPLETED, messageList.getMessages().get(3).getExchangeEventType());

    // Verify other message properties
    Message firstMessage = messageList.getMessages().get(0);
    assertEquals("id1", firstMessage.getExchangeId());
    assertEquals("body1", firstMessage.getMessageBody());
    assertEquals("headers1", firstMessage.getHeaders());
    assertEquals(TEST_ROUTE_ID_1, firstMessage.getRouteId());
    assertEquals("endpoint1", firstMessage.getEndpoint());
    assertEquals("endpointId1", firstMessage.getEndpointId());
    assertEquals(MessageType.REQUEST, firstMessage.getMessageType());
    assertNull(firstMessage.getException());
    assertNotNull(firstMessage.getTimeStamp());
  }

  @Test
  void getMessagesShouldHandleMessagesWithNullEventType() {
    // Arrange
    List<Message> mockMessages = Arrays.asList(
        new Message("id1", null, "body1", "headers1", TEST_ROUTE_ID_1, "endpoint1", "endpointId1", MessageType.REQUEST, null)
    );
    when(messageService.getMessageList()).thenReturn(mockMessages);

    // Act
    Response response = contextController.getMessages();

    // Assert
    assertEquals(200, response.getStatus());
    MessageList messageList = (MessageList) response.getEntity();
    assertNotNull(messageList);
    assertEquals(1, messageList.getMessages().size());
    assertNull(messageList.getMessages().get(0).getExchangeEventType());
  }

  @Test
  void getMessagesShouldReturnEmptyListWhenNoMessages() {
    // Arrange
    when(messageService.getMessageList()).thenReturn(new ArrayList<>());

    // Act
    Response response = contextController.getMessages();

    // Assert
    assertEquals(200, response.getStatus());
    MessageList messageList = (MessageList) response.getEntity();
    assertNotNull(messageList);
    assertTrue(messageList.getMessages().isEmpty());
  }

  @Test
  void deleteMessagesShouldResetMessageService() {
    // Act
    Response response = contextController.deleteMessages();

    // Assert
    assertEquals(200, response.getStatus());
    assertEquals("deleted.", response.getEntity());
    verify(messageService).reset();
  }

  @Test
  void getWidgetsShouldHandleEmptyRoutes() {
    // Arrange
    when(routeContextService.getCamelRoutes()).thenReturn(new ArrayList<>());
    when(camelContext.getName()).thenReturn("TestContext");
    when(camelContext.getVersion()).thenReturn("3.18.0");

    // Act
    Response response = contextController.getWidgets();

    // Assert
    assertEquals(200, response.getStatus());
    CamelBeeContext context = (CamelBeeContext) response.getEntity();
    assertNotNull(context);
    assertTrue(context.getRoutes().isEmpty());
  }

  @Test
  void getWidgetsShouldHandleNullRouteOutputs() {
    // Arrange
    List<CamelRoute> mockRoutes = Arrays.asList(
        new CamelRoute(TEST_ROUTE_ID_1, "direct:start1", null, false, "direct:error1")
    );

    when(routeContextService.getCamelRoutes()).thenReturn(mockRoutes);
    when(camelContext.getName()).thenReturn("TestContext");
    when(camelContext.getVersion()).thenReturn("3.18.0");

    // Act
    Response response = contextController.getWidgets();

    // Assert
    assertEquals(200, response.getStatus());
    CamelBeeContext context = (CamelBeeContext) response.getEntity();
    assertNotNull(context);
    assertEquals(1, context.getRoutes().size());
    assertNull(context.getRoutes().get(0).getOutputs());
  }

  @Test
  void getWidgetsShouldIncludeSystemProperties() {
    // Arrange
    when(routeContextService.getCamelRoutes()).thenReturn(new ArrayList<>());
    when(camelContext.getName()).thenReturn("TestContext");
    when(camelContext.getVersion()).thenReturn("3.18.0");

    // Act
    Response response = contextController.getWidgets();

    // Assert
    assertEquals(200, response.getStatus());
    CamelBeeContext context = (CamelBeeContext) response.getEntity();
    assertNotNull(context);

  }
}
