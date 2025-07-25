package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TestRunnerServiceImpl implements TestRunnerService {

    private final TestService testService;

    private final IOService ioService;

    @Override
    public void run() {
        try {
            testService.executeTest();
        } catch (Exception e) {
            String userMessage = "An error occurred. Please try again later.";
            log.error("Error: {}", e.getMessage(), e);
            ioService.printLine(userMessage);
        }
    }
}
