package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        List<Question> questions = questionDao.findAll();
        TestResult testResult = new TestResult(student);

        for (Question question : questions) {
            processQuestion(question, testResult);
        }
        return testResult;
    }

    private void processQuestion(Question question, TestResult testResult) {
        ioService.printLine(question.text());
        List<Answer> answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("%d: %s", i + 1, answers.get(i).text());
        }
        int userChoice = ioService.readIntForRangeWithPrompt(
                1, answers.size(),
                "Your answer (number):",
                "Incorrect number, try again!"
        ) - 1;
        boolean isRight = answers.get(userChoice).isCorrect();
        testResult.applyAnswer(question, isRight);
    }
}
