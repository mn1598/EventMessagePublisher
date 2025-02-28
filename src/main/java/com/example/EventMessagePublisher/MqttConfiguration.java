package com.example.EventMessagePublisher;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

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

    @Bean
    public MqttPahoClientFactory mqttClient() throws MqttException {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] {brokerUrl});
        options.setUserName(brokerUsername);
        options.setPassword(brokerPassword.toCharArray());
        options.setCleanSession(false); // durable subscriptions
        factory.setConnectionOptions(options);

        return factory;
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
