package ru.otus.hw.controllers.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateDto {
    @NotBlank
    private String title;

    @NotBlank
    private String authorId;

    @NotBlank
    private String genreId;
}