package ru.otus.hw.configs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.TestPropertySource;
import ru.otus.hw.models.BrewedCoffee;
import ru.otus.hw.models.CoffeeOrder;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.main.web-application-type=none"
})
class CoffeeIntegrationConfigTest {

    @Autowired
    private MessageChannel coffeeInputChannel;

    @Test
    void shouldTransformEspressoOrderToBrewedCoffee() {
        var order = new CoffeeOrder("espresso");

        var replyChannel = new QueueChannel();
        var message = MessageBuilder.withPayload(order)
                .setReplyChannel(replyChannel)
                .build();

        coffeeInputChannel.send(message);

        var reply = (BrewedCoffee) Objects.requireNonNull(replyChannel.receive(4000)).getPayload();
        assertNotNull(reply, "Reply should not be null");
        assertEquals("espresso_roasted_ground", reply.getType());
        assertEquals("Espresso Ready", reply.getStatus());
    }

    @Test
    void shouldTransformCappuccinoOrderToBrewedCoffee() {
        var order = new CoffeeOrder("cappuccino");

        var replyChannel = new QueueChannel();
        var message = MessageBuilder.withPayload(order)
                .setReplyChannel(replyChannel)
                .build();

        coffeeInputChannel.send(message);

        var reply = (BrewedCoffee) Objects.requireNonNull(replyChannel.receive(4000)).getPayload();
        assertNotNull(reply, "Reply should not be null");
        assertEquals("cappuccino_roasted_ground", reply.getType());
        assertEquals("Cappuccino Ready", reply.getStatus());
    }

    @Test
    void shouldHandleUnknownType() {
        var order = new CoffeeOrder("test");

        var replyChannel = new QueueChannel();
        var message = MessageBuilder.withPayload(order)
                .setReplyChannel(replyChannel)
                .build();

        coffeeInputChannel.send(message);

        var reply = (BrewedCoffee) Objects.requireNonNull(replyChannel.receive(4000)).getPayload();
        assertNotNull(reply, "Reply should not be null");
        assertEquals("unknown", reply.getType());
        assertEquals("Error: Unable to process order", reply.getStatus());
    }
}