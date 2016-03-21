package com.epam.ta.openday.test;

import com.epam.ta.openday.FilterBasedListener;
import com.epam.ta.openday.RabbitMQConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test for Filter-based listener
 *
 * @author Andrei Varabyeu
 */
@ContextConfiguration(
        classes = { RabbitMQConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class FilterBasedValidatorTest {

    @Autowired
    private FilterBasedListener filterBasedListener;

    @Test
    public void testMessaging() {
        FilterBasedListener.Validator validator = filterBasedListener.createValidator(message -> {
            byte[] body = message.getBody();
            return null != body && Constants.EXPECTED_MESSAGE.equals(new String(body));
        });
        validator.waitForMessage(Constants.DEFAULT_TIMEOUT);
    }

}
