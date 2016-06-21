package com.epam.ta.openday;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.util.UUID;

/**
 * Registers itself as message reciever. Delegates message processing to subclasses
 *
 * @author Andrei Varabyeu
 */
public abstract class AbstractListener {

    /* Query Name/UUID for obtaining messages from RabbitMQ. Each time we need to create new queue */
    public static final String QUERY_UUID = UUID.randomUUID().toString();
    public static final String RABBIT_TRACE_EXCHANGE = "amq.rabbitmq.trace";
    public static final String ALL_PUBLISHED_MESSAGES_QUEUE = "publish.#";

    /**
     * Creates messaging validator
     *
     * @param rabbitAdmin RabbitMQ admin to register queue in RabbitMQ
     */
    public AbstractListener(RabbitAdmin rabbitAdmin) {

        /* create exclusive non-durable queue for obtaining messages */
        org.springframework.amqp.core.Queue queue =
                new org.springframework.amqp.core.Queue(QUERY_UUID, false, true, true);

        /* declare query */
        rabbitAdmin.declareQueue(queue);

        /* binds query to RabbitMQ logging topic */
        rabbitAdmin.declareBinding(
                BindingBuilder.bind(queue).to(new TopicExchange(RABBIT_TRACE_EXCHANGE)).with(
                        ALL_PUBLISHED_MESSAGES_QUEUE));

        rabbitAdmin.initialize();
    }

    /**
     * Asynchronously obtains messages from RabbitMQ
     *
     * @param message Message
     */
    @RabbitListener(queues = { "#{ T(com.epam.ta.openday.CacheBasedListener).QUERY_UUID}" })
    public final void handleMessage(Message message) {
        onMessage(message);
    }

    abstract protected void onMessage(Message message);
}
