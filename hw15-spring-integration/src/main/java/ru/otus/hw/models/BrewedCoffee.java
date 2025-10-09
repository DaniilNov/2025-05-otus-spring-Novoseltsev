package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrewedCoffee {
    private String type;

    private String status;

    @Override
    public String toString() {
        return "BrewedCoffee{" +
                "type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}