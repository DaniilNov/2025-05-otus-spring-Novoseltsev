package ru.otus.hw.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.BrewedCoffee;

@Slf4j
@Service
public class ErrorHandlingService {

    public void handleError(Message<?> message) {
        log.error("Error occurred: {}", message.getPayload(), message.getHeaders().getErrorChannel());

        MessageChannel replyChannel = (MessageChannel) message.getHeaders().getReplyChannel();
        if (replyChannel != null) {
            BrewedCoffee defaultResponse = new BrewedCoffee("unknown", "Error: Unable to process order");
            replyChannel.send(MessageBuilder.withPayload(defaultResponse).build());
        }
    }
}