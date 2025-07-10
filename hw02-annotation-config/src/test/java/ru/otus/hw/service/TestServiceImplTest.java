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
import ru.otus.hw.domain.Student;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

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
        Student student = new Student("Ivan", "Ivanov");

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPrompt(1, 2, "Your answer (number):", "Incorrect number, try again!"))
                .thenReturn(1);

        var testService = new TestServiceImpl(ioService, questionDao);
        testService.executeTestFor(student);

        InOrder inOrder = Mockito.inOrder(ioService);

        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");

        inOrder.verify(ioService).printLine("Is it a test question?");

        inOrder.verify(ioService).printFormattedLine("%d: %s", 1, "Yes");
        inOrder.verify(ioService).printFormattedLine("%d: %s", 2, "No");

        inOrder.verify(ioService).readIntForRangeWithPrompt(
                1, 2,
                "Your answer (number):",
                "Incorrect number, try again!"
        );
    }
}