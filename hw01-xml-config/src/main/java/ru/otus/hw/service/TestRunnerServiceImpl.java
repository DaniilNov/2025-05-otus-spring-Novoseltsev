package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.exceptions.ExceptionHandler;

@RequiredArgsConstructor
public class TestRunnerServiceImpl implements TestRunnerService {

    private final TestService testService;

    private final ExceptionHandler exceptionHandler;

    @Override
    public void run() {
        try {
            testService.executeTest();
        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }
}
