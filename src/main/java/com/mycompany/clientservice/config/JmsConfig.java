package com.mycompany.clientservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter); // usamos el único MessageConverter
        template.setReceiveTimeout(5000); // opcional
        return template;
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT); // JSON como String
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}



