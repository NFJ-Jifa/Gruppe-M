package com.gruppem.percentageservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ connection, message serialization, and queue declaration.
 */
@Configuration
public class RabbitConfiguration {

    // RabbitMQ connection properties
    @Value("${spring.rabbitmq.host}")     private String host;
    @Value("${spring.rabbitmq.port}")     private int port;
    @Value("${spring.rabbitmq.username}") private String user;
    @Value("${spring.rabbitmq.password}") private String pass;

    // Queue names (bound to energy.* properties)
    @Value("${energy.update-queue}")      private String updateQueue;
    @Value("${energy.final-queue}")       private String finalQueue;

    /**
     * Creates the RabbitMQ connection factory using caching for performance.
     */
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory(host, port);
        cf.setUsername(user);
        cf.setPassword(pass);
        return cf;
    }

    /**
     * Admin bean for declaring queues, exchanges, and bindings.
     */
    @Bean
    public AmqpAdmin amqpAdmin(CachingConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    /**
     * JSON message converter with Java time support and camelCase naming strategy.
     */
    @Bean
    public MessageConverter jsonConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // Handle Java 8+ time types (e.g. Instant)
                .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE); // e.g. "finalQueue"
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    /**
     * RabbitTemplate bean used for sending messages to queues.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            CachingConnectionFactory cf,
            MessageConverter converter) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(converter);
        return tpl;
    }

    /**
     * Factory to configure RabbitMQ listeners, including the JSON converter.
     * Ensures that @RabbitListener methods can consume JSON messages as Java objects.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            CachingConnectionFactory cf,
            MessageConverter converter) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(converter);
        return f;
    }

    /**
     * Declare the update queue (durable = true).
     */
    @Bean
    public Queue updateQueue() {
        return new Queue(updateQueue, true);
    }

    /**
     * Declare the final result queue (durable = true).
     */
    @Bean
    public Queue finalQueue() {
        return new Queue(finalQueue, true);
    }
}
