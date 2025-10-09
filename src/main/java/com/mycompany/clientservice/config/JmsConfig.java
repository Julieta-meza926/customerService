package com.mycompany.clientservice.config;

import com.mycompany.clientservice.model.dto.EventDTO;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@EnableJms
@Configuration
public class JmsConfig {



    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setReceiveTimeout(5000);
        return template;
    }

    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTypeIdPropertyName("_type");
        converter.setTargetType(MessageType.TEXT);

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("com.julieta.order.dto.EventDTO", EventDTO.class);

        converter.setTypeIdMappings(typeIdMappings);
        return converter;
    }

}