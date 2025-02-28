package com.example.EventMessagePublisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
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
