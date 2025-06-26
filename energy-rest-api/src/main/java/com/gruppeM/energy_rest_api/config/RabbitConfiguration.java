package com.gruppeM.energy_rest_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * This class defines the RabbitMQ configuration for the energy system.
 * It declares the queue used to receive raw energy messages from producers.
 */
@Configuration
public class RabbitConfiguration {

    @Value("${spring.rabbitmq.host}") private String host;
    @Value("${spring.rabbitmq.port}")      private int    port;
    @Value("${spring.rabbitmq.username}")  private String user;
    @Value("${spring.rabbitmq.password}")  private String pass;

    // RabbitMQ server connection configuration (read from properties)
    @Value("${energy.input-queue}")        private String inputQueue;
    @Value("${energy.update-queue}")       private String updateQueue;
    @Value("${energy.percentage-queue}")   private String percentageQueue;

    /**
     * Creates and configures a caching RabbitMQ connection factory.
     * This manages the connection to the RabbitMQ broker.
     */
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory(host, port);
        cf.setUsername(user);
        cf.setPassword(pass);
        return cf;
    }

    /**
     * Admin bean that allows declaration of queues and exchanges at runtime.
     */
    @Bean
    public AmqpAdmin amqpAdmin(CachingConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    /**
     * Configures a Jackson-based JSON message converter.
     * Supports Java 8 time types (e.g. Instant) and uses camelCase field names.
     */
    @Bean
    public MessageConverter jsonConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // supports Instant and other time types
                .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // use ISO 8601 format

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);

        // Customize type mapping for deserialization
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        // allow all packages for incoming messages
        typeMapper.setTrustedPackages("*");
        // infer type from listener method
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        converter.setAlwaysConvertToInferredType(true);

        return converter;
    }

    /**
     * Configures the RabbitTemplate with a JSON converter for publishing messages.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            CachingConnectionFactory cf,
            MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        return template;
    }

    /**
     * Listener container factory for consuming messages using JSON deserialization.
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

    // === Queue Definitions ===

    /**
     * Declares the input queue (receives raw energy messages from producer).
     */
    @Bean public Queue inputQueue(@Value("${energy.input-queue}") String name) {
        return new Queue(name, true); } // durable

    /**
     * Declares the update queue (used to broadcast updates to other services).
     */
    @Bean public Queue updateQueue(@Value("${energy.update-queue}") String name) {
        return new Queue(name, true); } // durable

    /**
     * Declares the percentage queue (used for messages related to energy distribution percentages).
     */
    @Bean public Queue percentageQueue(@Value("${energy.percentage-queue}") String name) {
        return new Queue(name, true); } // durable
}
