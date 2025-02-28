package com.example.eventmessagepublisher;

import org.springframework.boot.SpringApplication;

public class TestEventMessagePublisherApplication {

	public static void main(String[] args) {
		SpringApplication.from(EventMessagePublisherApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
