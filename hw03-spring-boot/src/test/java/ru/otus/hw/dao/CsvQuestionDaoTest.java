package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "test.locale=en-US",
})
@Import(CsvQuestionDao.class)
class CsvQuestionDaoTest {

    @Autowired
    private CsvQuestionDao dao;

    @Mock
    private TestFileNameProvider fileNameProvider;

    @BeforeEach
    void setUp() {
        when(fileNameProvider.getTestFileName()).thenReturn("test-questions.csv");
    }

    @Test
    void findAll_ShouldParseAllQuestionsCorrectly() {
        List<Question> questions = dao.findAll();

        assertThat(questions).hasSize(6);

        Question first = questions.get(0);
        assertThat(first.text()).isEqualTo("Is there life on Mars?");
        assertThat(first.answers()).containsExactly(
                new Answer("Science doesn't know this yet", true),
                new Answer("Certainly. The red UFO is from Mars. And green is from Venus", false),
                new Answer("Absolutely not", false)
        );

        Question last = questions.get(5);
        assertThat(last.text()).isEqualTo("Is Java a compiled language?");
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