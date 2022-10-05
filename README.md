# Message-oriented Middleware
### Julian Pichler
### Tobias Wecht


## Objectives
The aim of this project is to show how to integrate and configure RabbitMQ in Spring. Furthermore, it will be shown how to send messages to an exchange with Spring services and how to receive messages from queues. Both the publish-subscribe and the worker pattern will be implemented in order to be prepared for both problems.

## Architecture
![alt text](/pictures/architecture.jpg)
RabbitMQ - Server: http://localhost:15672

Producer:
A producer service was implemented that sends the input of the clients (producer scripts / simulation of IoT devices) to the RabbitMQ server's exchanges. Furthermore, exchanges, queues and bindings were defined in this service.

Producer Service Endpoints: 
<br>
http://localhost:8080/api/publish sends requests to the Topic Exchange
<br>
http://localhost:8080/api/broadcast sends requests to the fanout exchange


Consumer:
Two types of consumer were implemented. A logging service that outputs the messages of the logging queue on the console and a worker service that can be replicated as often as desired and persists the messages of the clac queue in a MySQL database.

Consumer Services Endpoints: 
<br>
consumer-log-service: http://localhost:7777/
<br>
consumer-calc-persist-service: http://localhost/ - Port is random to start multiple instances.<br>

## Implementation

### RabbitMQ Config in Spring
Definition von Exchanges und Queues
```java
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
```


Set bindings
```java
@Bean
public Binding topicBinding(Queue topicQueue, TopicExchange exchange) {
    return BindingBuilder.bind(topicQueue).to(exchange).with(ROUTING_KEY);
}

@Bean
public Binding fanoutBinding(Queue fanoutQueue, FanoutExchange exchange) {
    return BindingBuilder.bind(fanoutQueue).to(exchange);
}
```
Set Message Converter:
```java
@Bean
public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
}

@Bean
public AmqpTemplate template(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter());
    return template;
}
```
### Producer as REST-Service

Send Message to Topic-Exchange
```java
@PostMapping ("/publish")
public ResponseEntity publish(Integer number){
    template.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY, new Measurement(number));
    return ResponseEntity.ok().build();
}
```

Send Message to Fanout-Exchange
```java
@PostMapping ("/broadcast")
public ResponseEntity broadcast(Integer number){
    template.convertAndSend(MQConfig.FANOUT_EXCHANGE,"", new Measurement(number));
    return ResponseEntity.ok().build();
}
```

### Consumer

Manual Acknowledge Worker (consumer-calc-persist)

To manually confirm messages, the following entry must be added to the Spring configuration.
```properties
spring.rabbitmq.listener.simple.acknowledge-mode=manual
```
This worker responds to messages in the calc queue and stores the received measurements in the database. Once this is done, RabbitMQ is informed that the message has been successfully processed with ```java channel.basicAck(tag, false) ```.
```java
@RabbitListener(queues = {MQConfig.TOPIC_QUEUE})
public void listen(Measurement measurement, Channel channel,
                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    repository.save(measurement);
    System.out.println("PERSISTING: "+measurement.getValue());
    channel.basicAck(tag, false);
}
```

Auto Acknowledge Worker (consumer-log)

This worker reacts to messages in the fanout queue and simply writes out the received message to the console. The acknowledge mode "AUTO" is used here, which is used by default in Spring. This sends an acknowledge to RabbitMQ if the "listener method" was executed without an exception.
```java
@RabbitListener(queues = MQConfig.FANOUT_QUEUE)
public void listen(Measurement measurement){
    System.out.println("IM LOGGING: "+measurement.getValue());
}
```



## Result
When sending a request to the /publish endpoint of the Provider Service and then forwarding it to the Topic Exchange of RabbitMQ, the messages are split between the Worker(Persist) Services but not sent to the Logging Service.<br>.


Beispiel:<br>
.\start.ps1 -workers 3     //starts entire system with 3 workers<br>
.\producer.ps1 -mode 1 -runs 6   // mode 1 == publish <br>


![alt text](/pictures/send_publish.jpg)<br>
Worker 1:<br>
![alt text](/pictures/pub_1.jpg)<br>
Worker 2:<br>
![alt text](/pictures/pub_2.jpg)<br>
Worker 3:<br>
![alt text](/pictures/pub_3_1.jpg)<br>
![alt text](/pictures/pub_3.JPG)<br>

If all queues are to receive a message, it must be sent via the /broadcast endpoint of the provider service and forwarded to the RabbitMQ fanout exchange.
The fanout exchange forwards the message to all bound queues and thus the logging service also receives the messages.

Example:<br>
.\start.ps1 //starts entire system with one worker<br>
.\producer.ps1 -mode 2 -runs 6   // mode 2 == broadcast <br>

![alt text](/pictures/send_broad.jpg)<br>
Logging Service:<br>
![alt text](/pictures/broad_log.jpg)<br>
Persist Service (Worker):<br>
![alt text](/pictures/broad_persist.jpg)<br>

## Conclusion
Setting up RabbitMQ with Docker and creating exchanges and queues in Spring went smoothly. Especially in the consumer, hardly any configuration was necessary for the integration of RabbitMQ and it could be elegantly solved with annotation. We see RabbitMQ's strength, as the slogan "Messaging that just works" promises, in the simple distribution of tasks, which can be perfectly integrated into a microservice architecture. It is suitable for both publish-subscribe and multiple-worker scenarios.

## Installation guide
Run the start.ps1 script, this builds the web services jars and then runs the docker-compose.


Start Script:
<br>
.\start.ps1


Arguments:
<ul>
  <li>-workers (Number of Persist-Services as int (default = 1))</li>
</ul>


Examples:
.\start.ps1 -workers 3

<br>
Database (MySQL):

calc-db: jdbc:mysql://localhost:3306/calc_db
<br>
Credentials in Properties

<br>
Consumer/Worker:
<br>
consumer-log-service: http://localhost:7777/
<br>
consumer-calc-persist-service: http://localhost/ - Port is random to start multiple instances.<br>

<br>
Producer:
<br>
producer-service: http://localhost:8080/
<br>
producer script:
Run Producer Script (Simulation of IoT-Device):
<br>
.\producer.ps1

Arguments:
<ul>
  <li>-runs (Number of Requests as int (default = 10))</li>
  <li>-sleep (Sleep in Milliseconds (default = 1000))</li>
  <li>-mode (1 = Publish (default), 2 = Broadcast)</li>
</ul>
<br>
Examples:
<br>
.\producer.ps1 -runs 100 -sleep 3000 -mode 1
<br>
.\producer.ps1 -runs 1000 -sleep 500 -mode 2


