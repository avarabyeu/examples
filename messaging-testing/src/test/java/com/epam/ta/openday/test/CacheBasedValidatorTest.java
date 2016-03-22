package com.epam.ta.openday.test;

import com.epam.ta.openday.CacheBasedListener;
import com.epam.ta.openday.RabbitMQConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test for on Cache-based listener
 *
 * @author Andrei Varabyeu
 */
@ContextConfiguration(
        classes = { RabbitMQConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class CacheBasedValidatorTest {

    @Autowired
    private CacheBasedListener cacheBasedValidator;

    @Test
    public void testMessaging() {
        cacheBasedValidator.waitForMessage(message -> {
            byte[] body = message.getBody();
            return null != body && Constants.EXPECTED_MESSAGE.equals(new String(body));
        }, Constants.DEFAULT_TIMEOUT);
    }

}
