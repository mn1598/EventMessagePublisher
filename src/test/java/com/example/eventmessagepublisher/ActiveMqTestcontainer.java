package com.example.eventmessagepublisher;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.testcontainers.activemq.ActiveMQContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class ActiveMqTestcontainer {

    public static final ActiveMQContainer ACTIVE_MQ_CONTAINER;
    public static final Network NETWORK = Network.newNetwork();
    public static final String BROKER_URL;
    public static final Integer ACTIVE_MQ_PORT = 1883;

    static {
        ACTIVE_MQ_CONTAINER = new ActiveMQContainer("apache/activemq-classic:5.18.3")
                .withNetwork(NETWORK)
                .withExposedPorts(ACTIVE_MQ_PORT)
                .withNetworkAliases("activemq")
                .waitingFor(Wait.forLogMessage(".*Connector mqtt started\\n", 1));
        ACTIVE_MQ_CONTAINER.start();
        await().atMost(Duration.ofSeconds(60)).until(ACTIVE_MQ_CONTAINER::isRunning);

        BROKER_URL = "tcp://" + ACTIVE_MQ_CONTAINER.getHost() + ":" + ACTIVE_MQ_CONTAINER.getMappedPort(ACTIVE_MQ_PORT);
    }

    @AfterAll
    public static void teardown() {
        ACTIVE_MQ_CONTAINER.stop();
    }

    private ActiveMqTestcontainer() {
        // private constructor to prevent instantiation
    }
}
