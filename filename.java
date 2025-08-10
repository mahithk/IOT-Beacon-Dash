
package com.iotdashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@SpringBootApplication
@EnableWebSocket
public class IoTDashboardApplication implements WebSocketConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(IoTDashboardApplication.class, args);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DeviceWebSocketHandler(), "/ws/devices")
                .setAllowedOrigins("*");
    }
}