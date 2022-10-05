package fhhgb.consumer.consumercalcpersist.worker;

import com.rabbitmq.client.Channel;
import fhhgb.consumer.consumercalcpersist.config.MQConfig;
import fhhgb.consumer.consumercalcpersist.model.Measurement;
import fhhgb.consumer.consumercalcpersist.repository.MeasurementRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RabbitMQWorker {
    @Autowired
    private MeasurementRepository repository;

    @RabbitListener(queues = {MQConfig.TOPIC_QUEUE})
    public void listen(Measurement measurement, Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        repository.save(measurement);
        System.out.println("PERSISTING: "+measurement.getValue());
        channel.basicAck(tag, false);
    }

}
