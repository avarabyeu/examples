package com.epam.ta.openday;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * Configuration
 *
 * @author Andrei Varabyeu
 */
@SpringBootApplication
@Import(Application.WebSocketConfig.class)
@EnableRabbit
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Configuration
    @EnableWebSocketMessageBroker
    public static class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

        @Autowired
        private Environment env;

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker(env.getRequiredProperty("config.websocket.topicOutcoming"));
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint(env.getRequiredProperty("config.websocket.endpoint")).withSockJS();
        }

    }

}
