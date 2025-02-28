package com.example.EventMessagePublisher;

import org.springframework.boot.SpringApplication;

public class TestEventMessagePublisherApplication {

	public static void main(String[] args) {
		SpringApplication.from(EventMessagePublisherApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
