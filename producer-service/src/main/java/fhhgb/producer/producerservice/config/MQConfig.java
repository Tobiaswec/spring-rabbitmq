package fhhgb.producer.producerservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class  MQConfig {

    public static final String ROUTING_KEY = "measurements";
    public static final String TOPIC_EXCHANGE = "topicExchange";
    public static final String FANOUT_EXCHANGE = "fanoutExchange";
    public static final String TOPIC_QUEUE = "calcQueue";
    public static final String FANOUT_QUEUE = "loggingQueue";

    @Bean
    public Queue topicQueue() {
        return new Queue(TOPIC_QUEUE);
    }

    @Bean
    public Queue fanoutQueue() {
        return new Queue(FANOUT_QUEUE);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding topicBinding(Queue topicQueue, TopicExchange exchange) {

        return BindingBuilder.bind(topicQueue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding topicBinding2(Queue topicQueue, FanoutExchange exchange) {

        return BindingBuilder.bind(topicQueue).to(exchange);
    }

    @Bean
    public Binding fanoutBinding(Queue fanoutQueue, FanoutExchange exchange) {

        return BindingBuilder.bind(fanoutQueue).to(exchange);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    /*@Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        //template.setMessageConverter(messageConverter());
        return template;
    }*/

}
