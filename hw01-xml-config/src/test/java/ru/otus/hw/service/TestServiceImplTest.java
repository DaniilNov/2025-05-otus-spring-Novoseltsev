package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {
    private static final String LS = System.lineSeparator();

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @Test
    void shouldPrintQuestionsAndAnswersCorrectly() {
        Answer answer1 = new Answer("Yes", true);
        Answer answer2 = new Answer("No", false);
        Question question = new Question("Is it a test question?", List.of(answer1, answer2));
        List<Question> questions = List.of(question);

        when(questionDao.findAll()).thenReturn(questions);

        TestServiceImpl testService = new TestServiceImpl(ioService, questionDao);

        testService.executeTest();

        String expectedOutput = "Is it a test question?" + LS +
                " - Yes" + LS +
                " - No" + LS +
                LS;

        InOrder inOrder = Mockito.inOrder(ioService);
        inOrder.verify(ioService).printLine(expectedOutput);
    }
}