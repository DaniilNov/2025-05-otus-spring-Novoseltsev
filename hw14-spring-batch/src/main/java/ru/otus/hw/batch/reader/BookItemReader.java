package ru.otus.hw.batch.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.Book;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookItemReader implements ItemReader<Book> {

    private final MongoTemplate mongoTemplate;

    private Iterator<Book> bookIterator;

    private int skip = 0;

    private final int limit = 10;

    private boolean hasMoreData = true;

    @Override
    public Book read() {
        if (bookIterator == null || (!bookIterator.hasNext() && hasMoreData)) {
            loadNextBatch();
        }

        return bookIterator.hasNext() ? bookIterator.next() : null;
    }

    private void loadNextBatch() {
        Query query = new Query()
                .skip(skip)
                .limit(limit)
                .with(Sort.by(Sort.Direction.ASC, "_id"));

        List<Book> books = mongoTemplate.find(query, Book.class);
        bookIterator = books.iterator();
        hasMoreData = books.size() == limit;
        skip += limit;
    }
}