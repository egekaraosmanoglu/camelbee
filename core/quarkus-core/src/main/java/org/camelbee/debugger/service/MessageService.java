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

package org.camelbee.debugger.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.camelbee.debugger.model.exchange.Message;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * MessageService.
 */
@ApplicationScoped
public class MessageService {

  private final long maxTracedMessageCount;

  private List<Message> messageList = new CopyOnWriteArrayList<>();

  public List<Message> getMessageList() {
    return messageList;
  }

  /**
   * Constructor.
   *
   * @param maxTracedMessageCount The maxTracedMessageCount.
   */
  public MessageService(@ConfigProperty(name = "camelbee.tracer-max-messages-count", defaultValue = "1000") long maxTracedMessageCount) {
    this.maxTracedMessageCount = maxTracedMessageCount;
  }

  /**
   * Add message to the messageList for
   * the CamelBee WebGl application.
   *
   * @param message The message.
   */
  public void addMessage(Message message) {
    if (message != null && maxTracedMessageCount > messageList.size()) {
      messageList.add(message);
    }
  }

  public void reset() {
    messageList.clear();
  }

}
