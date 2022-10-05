# Message-oriented Middleware
### Julian Pichler
### Tobias Wecht


## Zielsetzung
Ziel dieses Projekts ist es, zu zeigen, wie man RabbitMQ in Spring integriert und konfiguriert. Weiters soll gezeigt werden wie man mit Spring-Services Nachrichten an eine Exchange senden , bzw. Nachrichten von Queues empfangen kann. Dabei wird sowohl das Publish-Subscribe, als auch das Worker-Pattern umgesetzt, um für beide Problemstellungen gewappnet zu sein.

## Architektur
![alt text](/pictures/architecture.jpg)
RabbitMQ - Server: http://localhost:15672

Producer:
Es wurde ein Producer Service implementiert, dass den Input der Clients(Producer-Skripts / Simulation von IoT-Devices) an die Exchanges des RabbitMQ Servers sendet. Weiters wurden Exchanges, Queues und Bindings in diesem Service definiert.

Producer Service Endpoints: 
<br>
http://localhost:8080/api/publish sendet Requests an die Topic Exchange
<br>
http://localhost:8080/api/broadcast sendet Requests an die Fanout Exchange


Consumer:
Es wurden zwei Arten von Consumer implementiert. Ein Logging Service, dass die Nachrichten der Logging-Queue auf der Console ausgibt und ein Worker Service, das beliebig oft repliziert werden kann und die Nachrichten der Clac-Queue in einer MySQL Datenbank persistiert.

Consumer Services Endpoints: 
<br>
consumer-log-service: http://localhost:7777/
<br>
consumer-calc-persist-service: http://localhost/ - Port is random to start multiple instances.<br>

## Umsetzung

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


Bindings setzen
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
Message Converter setzten:
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

Um manuell Nachrichten zu bestätigen, muss in der Konfiguration von Spring folgender Eintrag ergänzt werden.
```properties
spring.rabbitmq.listener.simple.acknowledge-mode=manual
```

Dieser Worker reagiert auf Nachrichten in der Calc-Queue und speichert die empfangenen Measurements in der Datenbank. Ist dies geschehen, wird RabbitMQ mitgeteilt, dass die Nachricht erfolgreich verarbeitet wurde mit ```java channel.basicAck(tag, false) ```.
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

Dieser Worker reagiert auf Nachrichten in der Fanout-Queue und schreibt die erhaltene Nachricht einfach auf die Konsole aus. Hier wird der Acknowledge-Mode "AUTO" verwendet, welcher bei Spring standardmäßig verwendet wird. Dieser schickt ein Acknowledge an RabbitMQ, wenn die "Listener-Methode" ohne Exception ausgeführt wurde.
```java
@RabbitListener(queues = MQConfig.FANOUT_QUEUE)
public void listen(Measurement measurement){
    System.out.println("IM LOGGING: "+measurement.getValue());
}
```



## Ergebnis
Beim Senden von Request and den /publish Endpoint des Provider Service und die darauffolgenden Weiterleitung an die Topic Exchange von RabbitMQ, werden die Nachrichten auf die Worker(Persist) Services aufgeteilt, jedoch nicht an den Logging Service gesendet.<br>

Beispiel:<br>
.\start.ps1 -workers 3     //startet gesamtes System mit 3 Worker<br>
.\producer.ps1 -mode 1 -runs 6   // mode 1 == publish <br>


![alt text](/pictures/send_publish.jpg)<br>
Worker 1:<br>
![alt text](/pictures/pub_1.jpg)<br>
Worker 2:<br>
![alt text](/pictures/pub_2.jpg)<br>
Worker 3:<br>
![alt text](/pictures/pub_3_1.jpg)<br>
![alt text](/pictures/pub_3.JPG)<br>

Sollen nun alle Queues eine Nachricht bekommen muss diese über den /broadcast Endpoint des Provider Services gesendet und an die Fanout Exchange des RabbitMQ weitergeleitet werden.
Die Fanout Exchange leitet die Nachricht an alle binded Queues weiter und somit erhält auch das Logging Service die Nachrichten.

Beispiel:<br>
.\start.ps1 //startet gesamtes System mit einem Worker<br>
.\producer.ps1 -mode 2 -runs 6   // mode 2 == broadcast <br>

![alt text](/pictures/send_broad.jpg)<br>
Logging Service:<br>
![alt text](/pictures/broad_log.jpg)<br>
Persist Service (Worker):<br>
![alt text](/pictures/broad_persist.jpg)<br>

## Conclusion
Das Aufsetzen von RabbitMQ mit Docker und das Erstellen von Exchanges und Queues in Spring gestaltete, sich problemlos. Vorallem in den Consumer war für die Integration von RabbitMQ kaum eine Konfiguration notwendig und konnte elegant mit Annotation gelöst werden. RabbitMQs Stärke sehen wir, wie schon der Solgan "Messaging that just works" verspricht, in der simplen Verteilen von Aufgaben, die perfekt in eine Microservice-Architektur eingebunden werden kann. Es eignet sich sowohl für Publish-Subscribe- als auch Multiple-Worker-Szenarien.

## Installationsanleitung
Das start.ps1 Skript ausführen, dieses baut die Jars der Web Services und führt anschließend das docker-compose aus.


Start Skript:
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


