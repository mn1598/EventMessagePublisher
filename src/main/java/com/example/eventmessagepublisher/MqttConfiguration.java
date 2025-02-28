package com.example.eventmessagepublisher;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MqttConfiguration implements InitializingBean {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.broker.username}")
    private String brokerUsername;

    @Value("${mqtt.broker.password}")
    private String brokerPassword;

    @Value("${mqtt.publish.topic}")
    private String topic;

    @Bean
    public MqttPahoClientFactory mqttClient() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] {brokerUrl});
        options.setUserName(brokerUsername);
        options.setPassword(brokerPassword.toCharArray());
        options.setCleanSession(false); // durable subscriptions
        factory.setConnectionOptions(options);

        return factory;
    }

    @Bean
    public DirectChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(MqttPahoClientFactory mqttClient) {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler("publisherClient", mqttClient);
        handler.setAsync(true);
        handler.setDefaultQos(2);
        handler.setDefaultTopic(topic);
        return handler;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("------------------------");
        log.info("connected to MQTT broker");
        log.info("URL:{}", brokerUrl);
        log.info("USER:{}", brokerUsername);
        log.info("PASSWORD:{}", brokerPassword.replaceAll(".*", "*"));
        log.info("------------------------");
    }
}
