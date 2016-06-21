package com.epam.ta.openday;

import com.google.common.base.Preconditions;
import com.jayway.awaitility.Awaitility;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Created by avarabyeu on 3/21/16.
 */
public class FilterBasedListener extends AbstractListener {

    private List<Validator> filters = new CopyOnWriteArrayList<>();

    /**
     * Creates messaging validator
     *
     * @param rabbitAdmin RabbitMQ admin to register queue in RabbitMQ
     */
    public FilterBasedListener(RabbitAdmin rabbitAdmin) {
        super(rabbitAdmin);
    }

    public Validator createValidator(Predicate<Message> predicate) {
        Validator filter = new Validator(predicate);
        filters.add(filter);
        return filter;
    }

    @Override
    protected void onMessage(Message message) {
        filters.stream().filter(f -> f.filter.test(message)).forEach(f -> f.found.lazySet(true));
    }

    public class Validator {
        private final Predicate<Message> filter;
        private final AtomicBoolean found = new AtomicBoolean(false);

        public Validator(Predicate<Message> filter) {
            this.filter = filter;
        }

        /**
         * Waits for Messages to be appeared in message cache
         *
         * @param duration Amount of time to wait for the message
         */
        public final void waitForMessage(Duration duration) {
            Preconditions.checkArgument(null != filter, "Predicate shouldn't be null");
            Preconditions.checkArgument(null != duration, "Duration shouldn't be null");

            try {
                Awaitility.given().atMost(duration.toMillis(), TimeUnit.MILLISECONDS)
                        .pollDelay(com.jayway.awaitility.Duration.ONE_SECOND).until(found::get);
            } finally {
                //do not forget to remove filter!!!
                filters.remove(this);
            }
        }
    }
}
