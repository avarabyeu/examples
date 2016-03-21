package com.epam.ta.openday;

import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import com.jayway.awaitility.Awaitility;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class CacheBasedListener extends AbstractListener {

    /* Queue with messages */
    private final Queue<Message> cache;

    /**
     * Creates messaging validator
     *
     * @param rabbitAdmin RabbitMQ admin to register queue in RabbitMQ
     * @param cacheSize   Count of element to be kept in memory for validation
     */
    public CacheBasedListener(RabbitAdmin rabbitAdmin, Integer cacheSize) {
        super(rabbitAdmin);
        this.cache = Queues.synchronizedQueue(EvictingQueue.create(cacheSize));
    }

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
            List<Message> messages;
            //blocks receiving of messages until new copy is created
            synchronized (cache) {
                messages = new ArrayList<>(cache);
            }
            return messages.stream().filter(predicate).findAny().isPresent();

        });
    }

}
