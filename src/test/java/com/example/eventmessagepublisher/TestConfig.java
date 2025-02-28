package com.example.eventmessagepublisher;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class TestConfig {

    @Value("${mqtt.broker.username}")
    private String brokerUsername;

    @Value("${mqtt.broker.password}")
    private String brokerPassword;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(ActiveMqTestcontainer.BROKER_URL, MqttClient.generateClientId(),
                new MemoryPersistence());

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);
        connOpts.setUserName(brokerUsername);
        connOpts.setPassword(brokerPassword.toCharArray());
        client.connect(connOpts);
        return client;
    }

}
