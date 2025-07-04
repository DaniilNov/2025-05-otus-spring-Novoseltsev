package ru.otus.hw.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.service.IOService;

@Slf4j
@RequiredArgsConstructor
public class CommonExceptionHandler implements ExceptionHandler {

    private final IOService ioService;

    @Override
    public void handle(Exception e) {
        String userMessage = "An error occurred. Please try again later.";
        String logMessage = "Error: " + e.getMessage();

        log.error(logMessage, e);
        ioService.printLine(userMessage);
    }
}