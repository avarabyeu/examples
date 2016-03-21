package com.epam.ta.openday;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Use config uses Server's properties which means it connects to the Server's queue
 * Queue doesn't copy message to each consumer. So test client just steal message from the Server's queue
 *
 * @author Andrei Varabyeu
 */
@Configuration
@PropertySource("classpath:application.yml")
@EnableRabbit
public class StealRabbitConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigIn() throws IOException {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Component
    public static class Listener {

        @RabbitListener(bindings = @QueueBinding(
                value = @org.springframework.amqp.rabbit.annotation.Queue(value = "${config.rabbit.queue}", durable = "true"),
                exchange = @Exchange(value = "${config.rabbit.exchange}", type = ExchangeTypes.TOPIC, durable = "true")))
        public void onRabbitMessage(String message) {
            System.out.println("I'VE JUST STOLEN A MESSAGE!!! " + message);
        }
    }

}
