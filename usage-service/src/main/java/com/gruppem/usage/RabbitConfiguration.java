package com.gruppem.usage;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

@Configuration
public class RabbitConfiguration {

    @Value("${spring.rabbitmq.host}")      private String host;
    @Value("${spring.rabbitmq.port}")      private int    port;
    @Value("${spring.rabbitmq.username}")  private String user;
    @Value("${spring.rabbitmq.password}")  private String pass;
    @Value("${energy.input-queue}")        private String inputQueue;
    @Value("${energy.update-queue}")       private String updateQueue;

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory(host, port);
        cf.setUsername(user);
        cf.setPassword(pass);
        return cf;
    }

    @Bean
    public AmqpAdmin amqpAdmin(CachingConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    @Bean
    public MessageConverter jsonConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory cf,
                                         MessageConverter converter) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(converter);
        return tpl;
    }

    // Make sure to also register the converter for @RabbitListener-based receivers
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            CachingConnectionFactory cf,
            MessageConverter converter) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(converter);
        return f;
    }

    @Bean
    public Queue inputQueue() {
        return new Queue(inputQueue, true);
    }

    @Bean
    public Queue updateQueue() {
        return new Queue(updateQueue, true);
    }
}
