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

@Configuration
public class RabbitConfiguration {

    @Value("${spring.rabbitmq.host}")      private String host;
    @Value("${spring.rabbitmq.port}")      private int    port;
    @Value("${spring.rabbitmq.username}")  private String user;
    @Value("${spring.rabbitmq.password}")  private String pass;
    @Value("${energy.input-queue}")        private String inputQueue;
    @Value("${energy.update-queue}")       private String updateQueue;
    @Value("${energy.percentage-queue}")   private String percentageQueue;

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

    /**
     * JSON-конвертер, поддерживающий Instant и lowerCamelCase
     */
    @Bean
    public MessageConverter jsonConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        // доверяем всем incoming типам
        typeMapper.setTrustedPackages("*");
        // но всё равно инферим тип из сигнатуры слушателя
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        converter.setAlwaysConvertToInferredType(true);

        return converter;
    }





    @Bean
    public RabbitTemplate rabbitTemplate(
            CachingConnectionFactory cf,
            MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            CachingConnectionFactory cf,
            MessageConverter converter) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(converter);
        return f;
    }

    @Bean public Queue inputQueue(@Value("${energy.input-queue}") String name)        { return new Queue(name, true); }
    @Bean public Queue updateQueue(@Value("${energy.update-queue}") String name)      { return new Queue(name, true); }
    @Bean public Queue percentageQueue(@Value("${energy.percentage-queue}") String name) { return new Queue(name, true); }
}
