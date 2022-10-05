package fhhgb.producer.producerservice.controller;

import fhhgb.producer.producerservice.config.MQConfig;
import fhhgb.producer.producerservice.model.Measurement;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerController {

    @Autowired
    private RabbitTemplate template;

    @PostMapping ("/publish")
    public ResponseEntity publish(Integer number){
        template.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY, new Measurement(number));
        return ResponseEntity.ok().build();
    }

    @PostMapping ("/broadcast")
    public ResponseEntity broadcast(Integer number){
        template.convertAndSend(MQConfig.FANOUT_EXCHANGE,"", new Measurement(number));
        return ResponseEntity.ok().build();
    }
}
