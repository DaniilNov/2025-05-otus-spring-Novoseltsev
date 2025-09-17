package ru.otus.hw.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;

public interface BookCustomRepository {
    Mono<Book> findBookById(String id);

    Flux<Book> findAllBooks();

    Mono<Book> saveBook(Book book);
}
