package com.epam.ta.openday.test;

import com.epam.ta.openday.RabbitMQConfig;
import com.epam.ta.openday.StealRabbitConfig;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Demo of Stealing Messages
 *
 * @author Andrei Varabyeu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { RabbitMQConfig.class, StealRabbitConfig.class },
        initializers = ConfigFileApplicationContextInitializer.class)
public class StealingTest {

    @Autowired
    private StealRabbitConfig.Listener listener;

    @Test
    public void stealTest() throws InterruptedException {
        Awaitility.await().forever().pollDelay(Duration.TWO_SECONDS).until(() -> false);
    }
}
