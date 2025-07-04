package ru.otus.hw.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.service.IOService;

@Slf4j
@RequiredArgsConstructor
public class LoggingExceptionHandler implements ExceptionHandler {

    private final IOService ioService;

    @Override
    public void handle(Exception e) {
        String message;
        if (e instanceof QuestionReadException) {
            message = "Error reading questions from file: " + e.getMessage();
            log.error(message);
        } else {
            message = "An unexpected error occurred: " + e.getMessage();
            log.error(message, e);
        }
        ioService.printLine(message);
    }
}