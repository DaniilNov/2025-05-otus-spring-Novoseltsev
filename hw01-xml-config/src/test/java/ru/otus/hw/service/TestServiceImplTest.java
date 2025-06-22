package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.CsvQuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvQuestionDaoTest {

    @Test
    void shouldReadQuestionsFromCsvFileCorrectly() {
        TestFileNameProvider fileNameProvider = new AppProperties("questions.csv");
        CsvQuestionDao csvQuestionDao = new CsvQuestionDao(fileNameProvider);

        List<Question> questions = csvQuestionDao.findAll();

        assertThat(questions).hasSize(3);

        Question firstQuestion = questions.get(0);
        assertThat(firstQuestion.text()).isEqualTo("Is there life on Mars?");
        assertThat(firstQuestion.answers()).containsExactly(
                new Answer("Science doesn't know this yet", true),
                new Answer("Certainly. The red UFO is from Mars. And green is from Venus", false),
                new Answer("Absolutely not", false)
        );

        Question secondQuestion = questions.get(1);
        assertThat(secondQuestion.text()).isEqualTo("How should resources be loaded form jar in Java?");
        assertThat(secondQuestion.answers()).containsExactly(
                new Answer("ClassLoader#geResourceAsStream or ClassPathResource#getInputStream", true),
                new Answer("ClassLoader#geResource#getFile + FileReader", false),
                new Answer("Wingardium Leviosa", false)
        );

        Question thirdQuestion = questions.get(2);
        assertThat(thirdQuestion.text()).isEqualTo("Which option is a good way to handle the exception?");
        assertThat(thirdQuestion.answers()).containsExactly(
                new Answer("@SneakyThrow", false),
                new Answer("e.printStackTrace()", false),
                new Answer("Rethrow with wrapping in business exception (for example, QuestionReadException)", true),
                new Answer("Ignoring exception", false)
        );
    }
}