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
	public void onWebsocketMessage(String message) throws Exception {
		rabbitTemplate.convertAndSend(queueName, message);
	}

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue("${config.rabbit.queue}"),
			exchange = @Exchange(value = "${config.rabbit.exchange}", type = ExchangeTypes.TOPIC)))
	public void onRabbitMessage(String message) {
		websocketTemplate.convertAndSend(topicOutcoming, "Processed by server:" + message + "");
	}

}
