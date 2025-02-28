package com.example.EventMessagePublisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventMessagePublisherApplication {

	@Value("${mqtt.publish.topic}")
	private String topic;

	public void sendEventMessage() {
		System.out.println("Sending event message to topic: " + topic);
	}

	public static void main(String[] args) {
		SpringApplication.run(EventMessagePublisherApplication.class, args);
	}

}
