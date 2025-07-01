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

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @Test
    void shouldPrintQuestionsAndAnswers() {
        List<Answer> answers = List.of(
                new Answer("Yes", true),
                new Answer("No", false)
        );
        List<Question> questions = List.of(
                new Question("Is it a test question?", answers)
        );

        when(questionDao.findAll()).thenReturn(questions);

        TestServiceImpl testService = new TestServiceImpl(ioService, questionDao);

        testService.executeTest();

        InOrder inOrder = Mockito.inOrder(ioService);
        inOrder.verify(ioService).printLine("Is it a test question?");
        inOrder.verify(ioService).printFormattedLine(" - %s", "Yes");
        inOrder.verify(ioService).printFormattedLine(" - %s", "No");
        inOrder.verify(ioService).printLine("");
    }

}