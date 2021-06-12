package com.runinng_app.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

@SpringBootApplication
@Slf4j
public class ClientTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new SpringApplicationBuilder(ClientTest.class)
                .web(WebApplicationType.NONE)
                .run(args);

        var client = new StandardWebSocketClient();
        var stomCLient = new WebSocketStompClient(client);
        stomCLient.setMessageConverter(new StringMessageConverter());

        var connection = stomCLient.connect("ws://localhost:8080/chat", new StompSessionHandler() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/messages", this);
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.error("Communication error:", exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                log.error("Transport layer error:", exception);
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if ("/topic/messages".equals(headers.getDestination())) {
                    log.info(payload + "");
                }
            }
        });
        sleep(1000);

        try {
            // create `ObjectMapper` instance
            ObjectMapper mapper = new ObjectMapper();

            // create a JSON object
            ObjectNode user = mapper.createObjectNode();
            user.put("login", "Kuba");
            user.put("password", "123456");
            user.put("weight", "65.0");
            user.put("height", "179.0");
            user.put("sex", "male");
            user.put("password", "123456");

            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
            System.out.println(json);
            connection.get().send("/app/login", json);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String s = "";
        Scanner sc = new Scanner(System.in);

        do {
            s = sc.nextLine();
            connection.get().send("/app/chat", s);
        } while (!s.startsWith("q!"));
    }

}
