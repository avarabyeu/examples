package com.epam.ta.openday;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.net.URI;

@EnableRabbit
@Configuration
@PropertySource("classpath:test.properties")
public class RabbitMQConfig {

    @Value("${rabbit.uri}")
    private String rabbitUri;

    @Value("${rabbit.concurrent.consumers}")
    private Integer rabbitConcurrentConsumers;

    @Value("${rabbit.concurrent.consumers.max}")
    private Integer rabbitMacConcurrentConsumers;

    @Value("${message.validator.cache.size}")
    private Integer messageValidatorCacheSize;

    /**
     * Creates rabbitMQ connection factory
     *
     * @return Configured connection factory
     */
    @Bean(destroyMethod = "destroy")
    public CachingConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(URI.create(rabbitUri));
    }

    /**
     * @return RabbitMQ Listener Factory for Spring Framework
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(rabbitConcurrentConsumers);
        factory.setMaxConcurrentConsumers(rabbitMacConcurrentConsumers);
        factory.setAutoStartup(true);
        return factory;
    }

    /**
     * @return Rabbit Admin
     */
    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    @Lazy
    public CacheBasedListener cacheBasedListener() {
        return new CacheBasedListener(rabbitAdmin(), messageValidatorCacheSize);
    }

    @Lazy
    @Bean
    public FilterBasedListener filterBasedListener() {
        return new FilterBasedListener(rabbitAdmin());
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigIn() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
