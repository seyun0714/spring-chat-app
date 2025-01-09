package com.example.springchatapp.config;


import com.example.springchatapp.websocket.ChatWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocket chatWebSocket;

    @Autowired
    public WebSocketConfig(ChatWebSocket chatWebSocket) {
        this.chatWebSocket = chatWebSocket;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocket, "/ws/chat").setAllowedOrigins("*");
    }
}
