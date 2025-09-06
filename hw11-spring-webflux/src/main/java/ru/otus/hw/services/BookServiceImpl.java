package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    @Override
    public Mono<BookDto> findDtoById(String id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Book with id %s not found".formatted(id))))
                .flatMap(this::convertToDto);
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAll()
                .flatMap(this::convertToDto);
    }

    @Override
    public Mono<BookDto> insert(String title, String authorId, String genreId) {
        return save(null, title, authorId, genreId)
                .flatMap(this::convertToDto);
    }


    @Override
    public Mono<BookDto> update(String id, String title, String authorId, String genreId) {
        return save(id, title, authorId, genreId)
                .flatMap(this::convertToDto);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return commentRepository.deleteByBookId(id)
                .then(bookRepository.deleteById(id));
    }

    private Mono<Book> save(String id, String title, String authorId, String genreId) {
        Mono<Author> authorMono = authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Author with id %s not found".formatted(authorId))));

        Mono<Genre> genreMono = genreRepository.findById(genreId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Genre with id %s not found".formatted(genreId))));

        return Mono.zip(authorMono, genreMono)
                .then(Mono.defer(() -> {
                    Book book = new Book();
                    if (id != null && !id.isEmpty()) {
                        book.setId(id);
                    }
                    book.setTitle(title);
                    book.setAuthorId(authorId);
                    book.setGenreId(genreId);
                    return bookRepository.save(book);
                }));
    }

    private Mono<BookDto> convertToDto(Book book) {
        Mono<Author> authorMono = authorRepository.findById(book.getAuthorId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Author with id %s not found".formatted(book.getAuthorId()))));

        Mono<Genre> genreMono = genreRepository.findById(book.getGenreId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Genre with id %s not found".formatted(book.getGenreId()))));

        return Mono.zip(authorMono, genreMono)
                .map(tuple -> {
                    Author author = tuple.getT1();
                    Genre genre = tuple.getT2();

                    BookDto dto = new BookDto();
                    dto.setId(book.getId());
                    dto.setTitle(book.getTitle());
                    dto.setAuthor(author);
                    dto.setGenre(genre);
                    return dto;
                });
    }
}
