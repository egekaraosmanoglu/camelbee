package org.camelbee.debugger.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.camelbee.debugger.model.exchange.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class MessageService {

    private List<Message> messageList = new CopyOnWriteArrayList<>();

    public List<Message> getMessageList() {
        return messageList;
    }

    public void addMessage(Message message) {
        messageList.add(message);
    }

    public void reset() {
        messageList.clear();
    }

}
