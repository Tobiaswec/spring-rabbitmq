package fhhgb.consumer.consumerlog.worker;


import fhhgb.consumer.consumerlog.config.MQConfig;
import fhhgb.consumer.consumerlog.model.Measurement;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQWorker {


    @RabbitListener(queues = MQConfig.FANOUT_QUEUE)
    public void listen(Measurement measurement){
        System.out.println("IM LOGGING: "+measurement.getValue());
    }
}
