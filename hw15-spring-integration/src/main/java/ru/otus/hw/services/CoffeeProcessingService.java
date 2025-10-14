package ru.otus.hw.services;

import org.springframework.stereotype.Service;
import ru.otus.hw.models.BrewedCoffee;
import ru.otus.hw.models.CoffeeOrder;

@Service
public class CoffeeProcessingService {

    public String roastCoffee(CoffeeOrder order) {
        System.out.println("Roasting beans for " + order.getType() +
                " on thread: " + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return order.getType() + "_roasted";
    }

    public String grindCoffee(String roastedBeans) {
        System.out.println("Grinding " + roastedBeans +
                " on thread: " + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return roastedBeans + "_ground";
    }

    public BrewedCoffee brewEspresso(String groundCoffee) {
        System.out.println("Brewing espresso from " + groundCoffee +
                " on thread: " + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new BrewedCoffee(groundCoffee, "Espresso Ready");
    }

    public BrewedCoffee brewCappuccino(String groundCoffee) {
        System.out.println("Brewing cappuccino from " + groundCoffee +
                " on thread: " + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new BrewedCoffee(groundCoffee, "Cappuccino Ready");
    }
}