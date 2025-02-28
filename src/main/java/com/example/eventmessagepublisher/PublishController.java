package com.example.eventmessagepublisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PublishController {

    private final MessageChannel mqttOutboundChannel;

    @GetMapping("/publish")
    public void publish() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ClassPathResource classPathResource = new ClassPathResource("exampleEvent.json");
        StreamUtils.copy(classPathResource.getInputStream(), byteArrayOutputStream);
        mqttOutboundChannel.send(MessageBuilder.withPayload(byteArrayOutputStream.toString()).build());
    }
}
