package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;

import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestServiceImplTest.TestConfig.class)
class TestServiceImplTest {

    @Configuration
    @Import(TestServiceImpl.class)
    public static class TestConfig {
    }

    @MockitoBean
    private LocalizedIOService ioService;

    @MockitoBean
    private QuestionDao questionDao;

    @Autowired
    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new TestServiceImpl(ioService, questionDao);
    }

    @Test
    void shouldPrintQuestionsAndAnswersCorrectly() {
        Answer answer1 = new Answer("Yes", true);
        Answer answer2 = new Answer("No", false);
        Question question = new Question("Is it a test question?", List.of(answer1, answer2));
        Student student = new Student("Ivan", "Ivanov");

        when(questionDao.findAll()).thenReturn(List.of(question));
        when(ioService.readIntForRangeWithPromptLocalized(
                1, 2,
                "TestService.select.answer",
                "TestService.answer.number.out.of.range"
        )).thenReturn(1);

        testService.executeTestFor(student);

        InOrder inOrder = Mockito.inOrder(ioService);

        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printLineLocalized("TestService.answer.the.questions");
        inOrder.verify(ioService).printLine("");

        inOrder.verify(ioService).printLine("Is it a test question?");

        inOrder.verify(ioService).printFormattedLine("%d: %s", 1, "Yes");
        inOrder.verify(ioService).printFormattedLine("%d: %s", 2, "No");

        inOrder.verify(ioService).readIntForRangeWithPromptLocalized(
                1, 2,
                "TestService.select.answer",
                "TestService.answer.number.out.of.range"
        );
    }
}