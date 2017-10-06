package com.arip.websocket;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by Arip Hidayat on 10/3/2017.
 */
public class Application {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String TOKEN = "c2c44569-8ae9-41bf-bb2f-a3b50fb5bf93";
        String URL   = "ws://localhost:8080/chat";

        WebSocketClient client = new StandardWebSocketClient();

        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(client));

        SockJsClient sockJsClient = new SockJsClient(transports);

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("token", TOKEN);

        StompSessionHandler chatStompSessionHandler = new ChatStompSessionHandler();

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = stompClient.connect(URL, new WebSocketHttpHeaders(), stompHeaders, chatStompSessionHandler).get();
        session.subscribe("/topic/messages", chatStompSessionHandler);
        session.subscribe("/user/queue/messages", chatStompSessionHandler);


        System.out.println("Type message with format 'sender:message'");
        System.out.println("Type 'exit' to quite from application.\n\n");

        Scanner scanner = new Scanner(System.in);
        String input = "";

        do {
            String[] messages = input.split(":");
            if (messages.length == 2) {
                session.send("/app/chat/" + messages[0], new Message(messages[1]));
            }

            input = scanner.nextLine();
        }
        while (!input.equalsIgnoreCase("exit"));
    }
}
