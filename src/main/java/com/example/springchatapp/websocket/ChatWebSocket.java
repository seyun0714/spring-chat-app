package com.example.springchatapp.websocket;

import com.example.springchatapp.service.DynamoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Date;

@Component
public class ChatWebSocket extends TextWebSocketHandler {
    private final CopyOnWriteArraySet<WebSocketSession> clients = new CopyOnWriteArraySet<>();
    private final DynamoService dynamoService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ChatWebSocket(DynamoService dynamoService, ObjectMapper objectMapper) {
        this.dynamoService = dynamoService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        clients.add(session);
        System.out.println("Client connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try{
            String payload = message.getPayload();
            System.out.println("수신된 메시지 : " + payload);
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.get("type").asText();

            if(type.equals("init")){
                String userId = jsonNode.get("userId").asText();
                System.out.println("초기 연결, userId: " + userId);
                session.getAttributes().put("userId", userId);  // 세션에 userId 저장

                ObjectNode responseJson = new ObjectMapper().createObjectNode();
                responseJson.put("type", "systemEnter");
                responseJson.put("userId", userId);

                for (WebSocketSession client : clients) {
                    if (client.isOpen() && client != session) {
                        try {
                            client.sendMessage(new TextMessage(responseJson.toString()));
                        } catch (Exception e) {
                            System.err.println("메시지 전송 실패: " + e.getMessage());
                        }
                    }
                }
                return;  // 연결만 초기화하고 리턴
            }

            else if(type.equals("chat")){
                String userId = jsonNode.get("userId").asText();
                String text = jsonNode.get("text").asText();
                long timestamp = new Date().getTime();

                dynamoService.saveMessage(userId, text, timestamp);

                ObjectNode responseJson = (ObjectNode) jsonNode;
                responseJson.put("createAt", timestamp);

                for(WebSocketSession client : clients){
                    if(client.isOpen()){
                        client.sendMessage(new TextMessage(responseJson.toString()));
                    }
                }
            }
        } catch(Exception e){
            System.err.println("메시지 처리 중 오류 : " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = session.getAttributes().get("userId").toString();

        ObjectNode responseJson = new ObjectMapper().createObjectNode();
        responseJson.put("type", "systemLeave");
        responseJson.put("userId", userId);

        for (WebSocketSession client : clients) {
            if (client.isOpen() && client != session) {
                try {
                    client.sendMessage(new TextMessage(responseJson.toString()));
                } catch (Exception e) {
                    System.err.println("메시지 전송 실패: " + e.getMessage());
                }
            }
        }

        clients.remove(session);
        System.out.println("Client disconnected");
    }


}
