package com.epam.ta.openday.test;

import com.epam.ta.openday.StealRabbitConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

/**
 * Created by avarabyeu on 3/21/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { StealRabbitConfig.class },
        initializers = ConfigFileApplicationContextInitializer.class)
public class StealingTest {

    @Autowired
    private StealRabbitConfig.Listener listener;

    @Test
    public void stealTest() throws InterruptedException {
        Thread.sleep(TimeUnit.MINUTES.toMillis(5l));
    }
}
