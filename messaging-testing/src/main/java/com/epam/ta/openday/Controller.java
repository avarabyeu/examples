package com.epam.ta.openday;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.stream.IntStream;

@org.springframework.stereotype.Controller
public class Controller {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SimpMessagingTemplate websocketTemplate;

    @Value("${config.rabbit.queue}")
    private String queueName;

    @Value("${config.websocket.topicOutcoming}")
    private String topicOutcoming;

    @MessageMapping("${config.websocket.topicIncoming}")
    public void onWebsocketMessage(IncomingMessage message) throws Exception {
        IntStream.range(0, message.getCount()).parallel()
                .forEach((i) -> rabbitTemplate.convertAndSend(queueName, message.getUrl()));

    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("${config.rabbit.queue}"),
            exchange = @Exchange(value = "${config.rabbit.exchange}", type = ExchangeTypes.TOPIC)))
    public void onRabbitMessage(String message) {
        websocketTemplate.convertAndSend(topicOutcoming, "Processed by server:" + message + "");
    }

    public static class IncomingMessage {
        private String url;
        private Integer count;

        public IncomingMessage(String url, Integer count) {
            this.url = url;
            this.count = count;
        }

        public IncomingMessage() {
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

}
