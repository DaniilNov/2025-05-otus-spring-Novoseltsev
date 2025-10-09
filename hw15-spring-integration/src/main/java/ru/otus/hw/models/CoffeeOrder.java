package ru.otus.hw.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoffeeOrder {
    @NotBlank(message = "Coffee type cannot be blank")
    @Pattern(regexp = "espresso|cappuccino", message = "Invalid coffee type")
    private String type;
}