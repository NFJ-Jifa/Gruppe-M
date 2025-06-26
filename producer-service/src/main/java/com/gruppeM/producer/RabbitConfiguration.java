package com.gruppeM.producer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Value("${energy.input-queue}")
    private String inputQueueName;

    @Bean
    public CachingConnectionFactory connectionFactory(
            @Value("${spring.rabbitmq.host}") String host,
            @Value("${spring.rabbitmq.port}") int port,
            @Value("${spring.rabbitmq.username}") String user,
            @Value("${spring.rabbitmq.password}") String pass
    ) {
        // Configure and return a caching connection factory for RabbitMQ
        CachingConnectionFactory cf = new CachingConnectionFactory(host, port);
        cf.setUsername(user);
        cf.setPassword(pass);
        return cf;
    }

    @Bean
    public AmqpAdmin amqpAdmin(CachingConnectionFactory cf) {
        // Responsible for declaring exchanges, queues, and bindings at application startup
        return new RabbitAdmin(cf);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        // Configure JSON converter with support for Java time classes
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // Enables support for java.time types
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Write ISO-8601 dates
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // Make all fields visible
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory cf,
                                         Jackson2JsonMessageConverter converter) {
        // Create RabbitTemplate with JSON message converter
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(converter);
        return rt;
    }

    @Bean
    public Queue inputQueue() {
        // Declare a durable queue for producer input messages
        return new Queue(inputQueueName, true);
    }
}
