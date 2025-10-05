package ru.otus.hw.batch.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.Comment;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentItemReader implements ItemReader<Comment> {

    private final MongoTemplate mongoTemplate;

    private Iterator<Comment> commentIterator;

    private int skip = 0;

    private final int limit = 10;

    private boolean hasMoreData = true;

    @Override
    public Comment read() {
        if (commentIterator == null || (!commentIterator.hasNext() && hasMoreData)) {
            loadNextBatch();
        }

        return commentIterator.hasNext() ? commentIterator.next() : null;
    }

    private void loadNextBatch() {
        Query query = new Query()
                .skip(skip)
                .limit(limit)
                .with(Sort.by(Sort.Direction.ASC, "_id"));

        List<Comment> comments = mongoTemplate.find(query, Comment.class);
        commentIterator = comments.iterator();
        hasMoreData = comments.size() == limit;
        skip += limit;
    }
}