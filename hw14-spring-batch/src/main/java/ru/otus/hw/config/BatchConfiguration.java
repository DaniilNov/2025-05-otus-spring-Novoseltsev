package ru.otus.hw.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.MongoPagingItemReader;
import org.springframework.batch.item.data.builder.MongoPagingItemReaderBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.model.AuthorProcessingWrapper;
import ru.otus.hw.batch.model.BookProcessingWrapper;
import ru.otus.hw.batch.model.GenreProcessingWrapper;
import ru.otus.hw.batch.processor.AuthorItemProcessor;
import ru.otus.hw.batch.processor.BookItemProcessor;
import ru.otus.hw.batch.processor.CommentItemProcessor;
import ru.otus.hw.batch.processor.GenreItemProcessor;
import ru.otus.hw.batch.writer.AuthorItemWriter;
import ru.otus.hw.batch.writer.BookItemWriter;
import ru.otus.hw.batch.writer.GenreItemWriter;
import ru.otus.hw.models.mongo.Author;
import ru.otus.hw.models.mongo.Book;
import ru.otus.hw.models.mongo.Comment;
import ru.otus.hw.models.mongo.Genre;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfiguration {

    private final AuthorItemProcessor authorItemProcessor;

    private final GenreItemProcessor genreItemProcessor;

    private final BookItemProcessor bookItemProcessor;

    private final CommentItemProcessor commentItemProcessor;

    private final AuthorItemWriter authorItemWriter;

    private final GenreItemWriter genreItemWriter;

    private final BookItemWriter bookItemWriter;

    private final MongoTemplate mongoTemplate;

    @Value("${app.batch.chunk-size:10}")
    private int chunkSize;

    @Bean
    public Step migrateAuthorStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("migrateAuthorStep", jobRepository)
                .<Author, AuthorProcessingWrapper>chunk(chunkSize, transactionManager)
                .reader(authorItemReader())
                .processor(authorItemProcessor)
                .writer(authorItemWriter)
                .build();
    }

    @Bean
    public Step migrateGenreStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("migrateGenreStep", jobRepository)
                .<Genre, GenreProcessingWrapper>chunk(chunkSize, transactionManager)
                .reader(genreItemReader())
                .processor(genreItemProcessor)
                .writer(genreItemWriter)
                .build();
    }

    @Bean
    public Step migrateBookStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("migrateBookStep", jobRepository)
                .<Book, BookProcessingWrapper>chunk(chunkSize, transactionManager)
                .reader(bookItemReader())
                .processor(bookItemProcessor)
                .writer(bookItemWriter)
                .build();
    }

    @Bean
    public Step migrateCommentStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                   JpaItemWriter<ru.otus.hw.models.jpa.Comment> jpaCommentWriter) {
        return new StepBuilder("migrateCommentStep", jobRepository)
                .<Comment, ru.otus.hw.models.jpa.Comment>chunk(chunkSize, transactionManager)
                .reader(commentItemReader())
                .processor(commentItemProcessor)
                .writer(jpaCommentWriter)
                .build();
    }

    @Bean
    public Job migrateLibraryJob(JobRepository jobRepository,
                                 Step migrateAuthorStep,
                                 Step migrateGenreStep,
                                 Step migrateBookStep,
                                 Step migrateCommentStep) {
        return new JobBuilder("migrateLibraryJob", jobRepository)
                .start(migrateAuthorStep)
                .next(migrateGenreStep)
                .next(migrateBookStep)
                .next(migrateCommentStep)
                .build();
    }

    @Bean
    public MongoPagingItemReader<Author> authorItemReader() {
        return new MongoPagingItemReaderBuilder<Author>()
                .name("authorItemReader")
                .template(mongoTemplate)
                .targetType(Author.class)
                .jsonQuery("{}")
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .pageSize(chunkSize)
                .build();
    }

    @Bean
    public MongoPagingItemReader<Genre> genreItemReader() {
        return new MongoPagingItemReaderBuilder<Genre>()
                .name("genreItemReader")
                .template(mongoTemplate)
                .targetType(Genre.class)
                .jsonQuery("{}")
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .pageSize(chunkSize)
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

    @Bean
    public JpaItemWriter<ru.otus.hw.models.jpa.Comment> jpaCommentWriter(EntityManagerFactory emf) {
        return new JpaItemWriterBuilder<ru.otus.hw.models.jpa.Comment>()
                .entityManagerFactory(emf)
                .usePersist(true)
                .build();
    }
}