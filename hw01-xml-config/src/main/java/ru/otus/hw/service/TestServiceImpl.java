package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        List<Question> questions = questionDao.findAll();

        for (Question question : questions) {
            ioService.printLine(question.text());
            for (Answer answer : question.answers()) {
                ioService.printFormattedLine(" - %s", answer.text());
            }
            ioService.printLine("");
        }
    }
}
