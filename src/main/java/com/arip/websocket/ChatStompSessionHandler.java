package com.arip.websocket;

import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;

/**
 * Created by Arip Hidayat on 10/3/2017.
 */
public class ChatStompSessionHandler extends StompSessionHandlerAdapter {

    @Override
    public void afterConnected(StompSession session, StompHeaders headers) {
        String sender = headers.getFirst("user-name");
        session.send("/topic/messages", new Message(sender, "Join!"));
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        return Message.class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object payload) {
        Message msg = (Message) payload;
        System.out.println("You've got message form '" + msg.getFrom() + "' : " + msg.getContent());
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        System.out.println(headers.getFirst("message"));
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        if (exception instanceof ConnectionLostException) {
            System.out.println("Connection Lost!");
        }
    }
}
