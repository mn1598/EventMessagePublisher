package com.example.eventmessagepublisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.awaitility.Awaitility;
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
		String url = "http://localhost:" + port + "/publish";

		// Subscribe to testcontainer ActiveMQ topic
		AtomicReference<String> receivedMessage = new AtomicReference<>();
		mqttClient.subscribe(publishTopic, (topic, msg) -> receivedMessage.set(new String(msg.getPayload())));
		restTemplate.getForEntity(url, Void.class);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamUtils.copy(new ClassPathResource("exampleEvent.json").getInputStream(), out);
		String expected = out.toString();

		await().until(() -> receivedMessage.get() != null);
		assertEquals(expected, receivedMessage.get());
	}

	@Test
	void testDurableSubscriber() throws Exception {
		String url = "http://localhost:" + port + "/publish";
		AtomicReference<String> receivedMessage = new AtomicReference<>();

		// Step 1: Subscribe and receive messages
		mqttClient.subscribe(publishTopic, (topic, msg) ->
			receivedMessage.set(new String(msg.getPayload(), StandardCharsets.UTF_8)));

		// Step 2: Publish first message (while subscriber is connected)
		restTemplate.getForEntity(url, Void.class);

		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> receivedMessage.get() != null);
		assertFalse(receivedMessage.get().isEmpty());

		// Step 3: Save received message and disconnect
		String expectedMessage = receivedMessage.get();
		receivedMessage.set(null);

		mqttClient.disconnect();

		// Step 4: Publish another message while subscriber is disconnected
		restTemplate.getForEntity(url, Void.class);

		// Step 5: Reconnect and verify the message is received
		mqttClient.connect();
		mqttClient.subscribe(publishTopic, (topic, msg) ->
				receivedMessage.set(new String(msg.getPayload(), StandardCharsets.UTF_8)));

		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> receivedMessage.get() != null);
		assertEquals(expectedMessage, receivedMessage.get());
	}

}
