package com.epam.ta.openday.test;

import com.epam.ta.openday.CacheBasedValidator;
import com.epam.ta.openday.RabbitMQConfig;
import com.epam.ta.openday.StealRabbitConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * Created by avarabyeu on 3/21/16.
 */
@ContextConfiguration(
        classes = { RabbitMQConfig.class, StealRabbitConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class MessagingTest {

    @Autowired
    private CacheBasedValidator cacheBasedValidator;

    @Test
    public void testMessaging() {
        cacheBasedValidator.waitForMessage(new Predicate<Message>() {
            @Override
            public boolean test(Message message) {
                byte[] body = message.getBody();
                return null != body && "HELLO WORLD".equals(new String(body));
            }
        }, Duration.ofMinutes(5));
    }

}