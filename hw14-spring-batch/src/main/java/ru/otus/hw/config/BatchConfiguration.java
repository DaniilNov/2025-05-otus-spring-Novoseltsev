package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.MongoPagingItemReader;
import org.springframework.batch.item.data.builder.MongoPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.model.BookProcessingWrapper;
import ru.otus.hw.batch.processor.BookItemProcessor;
import ru.otus.hw.batch.processor.CommentItemProcessor;
import ru.otus.hw.batch.writer.BookItemWriter;
import ru.otus.hw.batch.writer.CommentItemWriter;
import ru.otus.hw.models.mongo.Book;
import ru.otus.hw.models.mongo.Comment;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfiguration {

    private final BookItemProcessor bookItemProcessor;

    private final BookItemWriter bookItemWriter;

    private final CommentItemProcessor commentItemProcessor;

    private final CommentItemWriter commentItemWriter;

    private final MongoTemplate mongoTemplate;

    @Value("${app.batch.chunk-size:10}")
    private int chunkSize;

    @Bean
    public Step migrateBookStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("migrateBookStep", jobRepository)
                .<ru.otus.hw.models.mongo.Book, BookProcessingWrapper>chunk(chunkSize, transactionManager)
                .reader(bookItemReader())
                .processor(bookItemProcessor)
                .writer(bookItemWriter)
                .build();
    }

    @Bean
    public Step migrateCommentStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("migrateCommentStep", jobRepository)
                .<ru.otus.hw.models.mongo.Comment, ru.otus.hw.models.jpa.Comment>chunk(chunkSize, transactionManager)
                .reader(commentItemReader())
                .processor(commentItemProcessor)
                .writer(commentItemWriter)
                .build();
    }

    @Bean
    public Job migrateLibraryJob(JobRepository jobRepository,
                                 Step migrateBookStep,
                                 Step migrateCommentStep) {
        return new JobBuilder("migrateLibraryJob", jobRepository)
                .start(migrateBookStep)
                .next(migrateCommentStep)
                .build();
    }

    @Bean
    public MongoPagingItemReader<Book> bookItemReader() {
        return new MongoPagingItemReaderBuilder<Book>()
                .name("bookItemReader")
                .template(mongoTemplate)
                .targetType(Book.class)
                .jsonQuery("{}")
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .pageSize(chunkSize)
                .build();
    }

    @Bean
    public MongoPagingItemReader<Comment> commentItemReader() {
        return new MongoPagingItemReaderBuilder<Comment>()
                .name("commentItemReader")
                .template(mongoTemplate)
                .targetType(Comment.class)
                .jsonQuery("{}")
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .pageSize(chunkSize)
                .build();
    }
}