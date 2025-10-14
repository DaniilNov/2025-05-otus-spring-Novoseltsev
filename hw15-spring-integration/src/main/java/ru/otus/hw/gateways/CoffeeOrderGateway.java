package ru.otus.hw.gateways;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.models.BrewedCoffee;
import ru.otus.hw.models.CoffeeOrder;

@MessagingGateway
public interface CoffeeOrderGateway {
    @Gateway(requestChannel = "coffeeInputChannel")
    BrewedCoffee processOrder(CoffeeOrder order);
}