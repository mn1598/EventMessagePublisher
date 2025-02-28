package com.example.eventmessagepublisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class EventMessagePublisherApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventMessagePublisherApplication.class, args);
	}

}
