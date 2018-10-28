package com.smarthome.sensorsimulator;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.util.UUID;

@SpringBootApplication
public class SensorSimulatorApplication {

    @Value("${mqtt-client.serverURI}")
    String mqttServerURI;

    @Value("${rabbit-hostname}")
    String rabbitHostname;

    public static void main(String[] args) {
        SpringApplication.run(SensorSimulatorApplication.class, args);
    }

    @PostConstruct
    public void publish() throws InterruptedException {
        String sensorName = "sensor4";
        RabbitAdmin admin = new RabbitAdmin(connectionFactory());
        Queue queue = new Queue("sensors_" + sensorName);
        TopicExchange topicExchange = new TopicExchange("amq.topic");
        Binding binding = BindingBuilder.bind(queue).to(topicExchange).with("sensors." + sensorName);
        //Binding binding = BindingBuilder.bind(queue).to(topicExchange).with("amq.topic");
        admin.declareQueue(queue);
        admin.declareExchange(topicExchange);
        admin.declareBinding(binding);


        try {
            MqttClient client = new MqttClient(mqttServerURI, sensorName);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("home");
            options.setPassword("home".toCharArray());
            client.connect(options);
            MqttMessage message = new MqttMessage();
            String id = UUID.randomUUID().toString();
            for (int i=0;i<100000000;i++) {
                /*String jsonMessage = sensorName + ":::{\"instanceId\":1,\"thingPar\":{\"id\":1}," +
                        "\"thing\": {\"id\":2},\"value\":\"" +
                        // new Double(Math.random()*100.0).toString() + "\"}";
                        new Integer(i).toString() + "\"}";*/
                String jsonMessage = "{\"id\":\"" + id + "\", \"value\": " + new Double(Math.random()*100.0).toString() + "}";
                message.setPayload(jsonMessage.getBytes());
                client.publish("sensors." + sensorName, message);
                Thread.sleep(5000);
            }
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    AmqpTemplate template;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory(rabbitHostname);
        //        new CachingConnectionFactory(rabbitHostname);
        connectionFactory.setUsername("home");
        connectionFactory.setPassword("home");
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }
}
