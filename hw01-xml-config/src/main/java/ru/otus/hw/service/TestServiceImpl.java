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
            String formattedQuestion = formatQuestion(question);
            ioService.printLine(formattedQuestion);
        }
    }

    private String formatQuestion(Question question) {
        StringBuilder sb = new StringBuilder();
        sb.append(question.text()).append("\n");

        for (Answer answer : question.answers()) {
            sb.append(" - ").append(answer.text()).append("\n");
        }

        sb.append("\n");
        return sb.toString();
    }
}
