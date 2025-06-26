package com.gruppem.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String user;

    @Value("${spring.rabbitmq.password}")
    private String pass;

    @Value("${energy.queue}")
    private String energyQueueName;

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory(host, port);
        cf.setUsername(user);
        cf.setPassword(pass);
        return cf;
    }

    @Bean
    public AmqpAdmin amqpAdmin(CachingConnectionFactory cf) {
        // Automatically creates all Queue beans at application startup
        return new RabbitAdmin(cf);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // Support for Java 8 date/time types
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Write dates in ISO-8601 format
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS); // Avoid exceptions on empty beans

        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // Serialize all fields
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory cf,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(converter); // Use JSON converter for message serialization
        return tpl;
    }

    @Bean
    public Queue energyQueue() {
        return new Queue(energyQueueName, true); // Durable queue for energy messages
    }
}
