package com.example.eventmessagepublisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StreamUtils;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({TestConfig.class, MqttConfiguration.class})
@SpringBootTest(classes = EventMessagePublisherApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = EventMessagePublisherApplicationTests.Initializer.class)
class EventMessagePublisherApplicationTests {

	@Autowired
	MqttClient mqttClient;

	TestRestTemplate restTemplate = new TestRestTemplate();

	@Value("${mqtt.publish.topic}")
	private String publishTopic;

	@LocalServerPort
	private int port;

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"mqtt.broker.url=tcp://" +
					ActiveMqTestcontainer.ACTIVE_MQ_CONTAINER.getHost() + ":" +
					ActiveMqTestcontainer.ACTIVE_MQ_CONTAINER.getMappedPort(1883)
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@Test
	void contextLoads() {
		assertNotNull(mqttClient);
	}

	@Test
	void testPublishMessage() throws MqttException, IOException {
		// Subscribe to testcontainer ActiveMQ topic
		AtomicReference<String> receivedMessage = new AtomicReference<>();
		mqttClient.subscribe(publishTopic, (topic, msg) -> receivedMessage.set(new String(msg.getPayload())));
		restTemplate.getForEntity("http://localhost:"+  port + "/publish", Void.class);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(new ClassPathResource("exampleEvent.json").getInputStream(), out);
		String expected = out.toString();

		await().until(() -> receivedMessage.get() != null);
		assertEquals(expected, receivedMessage.get());
	}

	@Test
	void testDurableSubscriber() throws MqttException {
		// Subscribe to testcontainer ActiveMQ topic
		AtomicReference<String> receivedMessage = new AtomicReference<>();
		mqttClient.subscribe(publishTopic, (topic, msg) -> receivedMessage.set(new String(msg.getPayload())));
		restTemplate.getForEntity("http://localhost:"+  port + "/publish", Void.class);

		await().until(() -> receivedMessage.get() != null);
		assertFalse(receivedMessage.get().isEmpty());

		// Save received message and disconnect
		String expected = receivedMessage.get();
		receivedMessage.set(null);

		mqttClient.disconnect();
		restTemplate.getForEntity("http://localhost:"+  port + "/publish", Void.class); // Publish another message, while subscriber is disconnected
		mqttClient.connect();

		await().until(() -> receivedMessage.get() != null);
		assertEquals(expected, receivedMessage.get());
	}

}
