package com.epam.ta.openday;

import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.jayway.awaitility.Awaitility;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.time.Duration;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class MessagingValidator {

    /* Query Name/UUID for obtaining messages from RabbitMQ. Each time we need to create new queue */
    public static final String QUERY_UUID = UUID.randomUUID().toString();
    public static final String RABBIT_TRACE_EXCHANGE = "amq.rabbitmq.trace";

    /* Queue with messages */
    private final Queue<Message> cache;

    /**
     * Creates messaging validator
     *
     * @param rabbitAdmin RabbitMQ admin to register queue in RabbitMQ
     * @param cacheSize   Count of element to be kept in memory for validation
     */
    public MessagingValidator(RabbitAdmin rabbitAdmin, Integer cacheSize) {
        this.cache = Queues.synchronizedQueue(EvictingQueue.create(cacheSize));

        /* create exclusive non-durable queue for obtaining messages */
        org.springframework.amqp.core.Queue queue =
                new org.springframework.amqp.core.Queue(QUERY_UUID, false, true, true);

        /* declare query */
        rabbitAdmin.declareQueue(queue);

        /* binds query to RabbitMQ logging topic */
        rabbitAdmin.declareBinding(
                BindingBuilder.bind(queue).to(new TopicExchange(RABBIT_TRACE_EXCHANGE)).with("publish.#"));

        rabbitAdmin.initialize();
    }

    /**
     * Asynchronously obtains messages from RabbitMQ
     *
     * @param message Message
     */
    @RabbitListener(queues = { "#{ T(com.epam.ta.openday.MessagingValidator).QUERY_UUID}" })
    public final void onMessage(Message message) {
        cache.offer(message);
    }

    /**
     * Waits for Messages to be appeared in message cache
     *
     * @param predicate Predicate to check message
     * @param duration  Amount of time to wait for the message
     */
    public final void waitForMessage(Predicate<Message> predicate, Duration duration) {
        Preconditions.checkArgument(null != predicate, "Predicate shouldn't be null");
        Preconditions.checkArgument(null != duration, "Duration shouldn't be null");

        Awaitility.given().atMost(duration.toMillis(), TimeUnit.MILLISECONDS)
                .pollDelay(com.jayway.awaitility.Duration.ONE_SECOND).until(() -> {
            return Lists.newArrayList(cache).stream().filter(predicate).findAny().isPresent();
        });
    }

}
