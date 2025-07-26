package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = CsvQuestionDao.class)
class CsvQuestionDaoTest {

    @MockitoBean
    private TestFileNameProvider fileNameProvider;

    @Autowired
    private CsvQuestionDao dao;

    @BeforeEach
    void setUp() {
        when(fileNameProvider.getTestFileName()).thenReturn("test-questions.csv");
    }

    @Test
    void findAll_ShouldParseAllQuestionsCorrectly() {
        List<Question> questions = dao.findAll();

        assertThat(questions).hasSize(6);

        Question first = questions.get(0);
        assertEquals("Is there life on Mars?", first.text());
        assertThat(first.answers()).containsExactly(
                new Answer("Science doesn't know this yet", true),
                new Answer("Certainly. The red UFO is from Mars. And green is from Venus", false),
                new Answer("Absolutely not", false)
        );

        Question second = questions.get(1);
        assertEquals("How should resources be loaded form jar in Java?", second.text());
        assertThat(second.answers()).containsExactly(
                new Answer("ClassLoader#geResourceAsStream or ClassPathResource#getInputStream", true),
                new Answer("ClassLoader#geResource#getFile + FileReader", false),
                new Answer("Wingardium Leviosa", false)
        );

        Question last = questions.get(5);
        assertEquals("Is Java a compiled language?", last.text());
        assertThat(last.answers()).containsExactly(
                new Answer("Yes, it is compiled to bytecode", true),
                new Answer("No, it's interpreted", false),
                new Answer("Only in Spring Boot", false)
        );
    }

    @Test
    void findAll_ShouldSkipFirstCommentLine() {
        List<Question> questions = dao.findAll();

        assertThat(questions)
                .noneMatch(q -> q.text().startsWith("# Добавить сюда своих вопросов"));
    }
}