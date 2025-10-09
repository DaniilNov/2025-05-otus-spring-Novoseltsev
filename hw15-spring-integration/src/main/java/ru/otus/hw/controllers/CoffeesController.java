package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.gateways.CoffeeOrderGateway;
import ru.otus.hw.models.BrewedCoffee;
import ru.otus.hw.models.CoffeeOrder;

@RestController
@RequiredArgsConstructor
public class CoffeesController {

    private final CoffeeOrderGateway gateway;

    @PostMapping("/order-coffee")
    public BrewedCoffee orderCoffee(@RequestBody CoffeeOrder order) {
        return gateway.processOrder(order);
    }
}