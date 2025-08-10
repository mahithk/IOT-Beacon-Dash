package com.iotdashboard;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private double temperature = 22.5;
    private double humidity = 45.0;
    private boolean lightOn = false;
    private boolean fanOn = false;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        sendInitialState(session);
        if (sessions.size() == 1) {
            startSimulation();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectNode json = mapper.readValue(message.getPayload(), ObjectNode.class);
            if (json.has("command")) {
                handleCommand(json.get("command").asText());
                broadcastState();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCommand(String command) {
        switch (command) {
            case "toggleLight":
                lightOn = !lightOn;
                break;
            case "toggleFan":
                fanOn = !fanOn;
                break;
        }
    }

    private void startSimulation() {
        scheduler.scheduleAtFixedRate(() -> {
            temperature += (Math.random() - 0.5) * 0.5;
            humidity += (Math.random() - 0.5) * 1.0;
            temperature = Math.max(15, Math.min(35, temperature));
            humidity = Math.max(20, Math.min(80, humidity));
            broadcastState();
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void sendInitialState(WebSocketSession session) {
        try {
            ObjectNode state = mapper.createObjectNode();
            state.put("temperature", temperature);
            state.put("humidity", humidity);
            state.put("lightOn", lightOn);
            state.put("fanOn", fanOn);
            session.sendMessage(new TextMessage(state.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastState() {
        ObjectNode state = mapper.createObjectNode();
        state.put("temperature", temperature);
        state.put("humidity", humidity);
        state.put("lightOn", lightOn);
        state.put("fanOn", fanOn);
        TextMessage message = new TextMessage(state.toString());
        sessions.values().forEach(session -> {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}