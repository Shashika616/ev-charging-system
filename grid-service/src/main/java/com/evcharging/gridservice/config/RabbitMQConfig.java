package com.evcharging.gridservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper; 

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "ev-charging-exchange";
    public static final String QUEUE_NAME = "grid-service.booking-queue";
    public static final String ROUTING_KEY = "booking.confirmed";
    
    public static final String DLQ_EXCHANGE_NAME = "grid-service-dlx";
    public static final String DLQ_NAME = "grid-service.booking-dlq";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE_NAME);
    }

    @Bean
    public Queue dlq() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding dlqBinding(Queue dlq, DirectExchange dlqExchange) {
        return BindingBuilder.bind(dlq).to(dlqExchange).with("dlq.grid.routing.key");
    }

    @Bean
    public Queue queue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLQ_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", "dlq.grid.routing.key");
        return QueueBuilder.durable(QUEUE_NAME).withArguments(args).build();
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Manually create the ObjectMapper bean to avoid the crash we saw earlier!
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}