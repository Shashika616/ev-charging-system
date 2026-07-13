package com.evcharging.stationservice.config;

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
    public static final String QUEUE_NAME = "station-service.booking-queue";
    public static final String ROUTING_KEY = "booking.confirmed";
    
    // DLQ (Dead Letter Queue) Configuration
    public static final String DLQ_EXCHANGE_NAME = "ev-charging-dlx";
    public static final String DLQ_NAME = "station-service.booking-dlq";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // Dead Letter Exchange (DLX) - receives messages that fail processing
    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE_NAME);
    }

    @Bean
    public Queue dlq() {
        // Durable = survives RabbitMQ restart
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding dlqBinding(Queue dlq, DirectExchange dlqExchange) {
        // Route failed messages to the DLQ using the booking ID as routing key
        return BindingBuilder.bind(dlq).to(dlqExchange).with("dlq.routing.key");
    }

    @Bean
    public Queue queue() {
        // Configure the main queue with dead-letter arguments
        Map<String, Object> args = new HashMap<>();
        
        // If a message is rejected (nacked) without requeue, send it to the DLX
        args.put("x-dead-letter-exchange", DLQ_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", "dlq.routing.key");
        
        // Optional: Set a TTL (Time To Live) for messages in the queue
        // If a message sits in the queue for more than 1 hour without being consumed, 
        // it will be sent to the DLQ
        // args.put("x-message-ttl", 3600000); // 1 hour in milliseconds
        
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

    @Bean
    public ObjectMapper objectMapper() {
       return new ObjectMapper();
   }    
}