package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBookRepository implements BookRepository {
    private final EntityManager em;

    @Override
    public Optional<Book> findById(long id) {
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b " +
                        "LEFT JOIN FETCH b.author " +
                        "LEFT JOIN FETCH b.genre " +
                        "WHERE b.id = :id",
                Book.class);
        query.setParameter("id", id);
        return query.getResultStream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        TypedQuery<Book> query = em.createQuery(
                "SELECT DISTINCT b FROM Book b " +
                        "LEFT JOIN FETCH b.author " +
                        "LEFT JOIN FETCH b.genre",
                Book.class);
        return query.getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);
            return book;
        }
        return em.merge(book);
    }

    @Override
    public void deleteById(long id) {
        Book book = em.find(Book.class, id);
        if (book != null) {
            em.remove(book);
        }
    }
}
