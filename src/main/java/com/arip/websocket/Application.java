package com.arip.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by Arip Hidayat on 10/3/2017.
 */
public class Application {

    private static String URL           = "http://localhost:8080";
    private static String CLIENT_ID     = "arip";
    private static String CLIENT_SECRET = "s3cret";

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        Scanner scanner = new Scanner(System.in);
        String username;
        String password;
        String chat = "";

        System.out.println("Input Username:");
        username = scanner.next();

        System.out.println("Input Password : ");
        password = scanner.next();

        ResponseEntity<String> response = login(username, password);
        String token = getToken(response);

        StompSessionHandler chatStompSessionHandler = new ChatStompSessionHandler();
        StompSession session = createWsConnection(token, chatStompSessionHandler);
        session.subscribe("/topic/messages", chatStompSessionHandler);
        session.subscribe("/user/queue/messages", chatStompSessionHandler);

        System.out.println("\n\nYou are logged in as '" + username + "'");
        System.out.println("Type message with format 'sender:message'");
        System.out.println("Type 'exit' to quite from application.\n\n");

        do {
            String[] messages = chat.split(":");
            if (messages.length == 2) {
                session.send("/app/chat/" + messages[0], new Message(messages[1]));
            }

            chat = scanner.nextLine();
        }
        while (!chat.equalsIgnoreCase("exit"));
    }

    private static StompSession createWsConnection(String token, StompSessionHandler stompSessionHandler) throws ExecutionException, InterruptedException {
        WebSocketClient client = new StandardWebSocketClient();

        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(client));

        SockJsClient sockJsClient = new SockJsClient(transports);

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("token", token);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        return stompClient.connect(URL + "/chat", new WebSocketHttpHeaders(), stompHeaders, stompSessionHandler).get();
    }

    private static ResponseEntity<String> login(String username, String password) {
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, createHeaders(CLIENT_ID, CLIENT_SECRET));

        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.postForEntity(URL + "/oauth/token", request , String.class);
    }

    private static String getToken(ResponseEntity<String> response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());

        return root.path("access_token").textValue();
    }

    private static HttpHeaders createHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
            setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }};
    }
}
