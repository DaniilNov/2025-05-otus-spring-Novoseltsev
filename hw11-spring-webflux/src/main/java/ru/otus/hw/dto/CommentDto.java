package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Book;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private String id;

    private String text;

    private Book book;
}
