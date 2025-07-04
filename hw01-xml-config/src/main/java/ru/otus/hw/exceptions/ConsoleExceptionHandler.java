package ru.otus.hw.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleExceptionHandler implements ExceptionHandler {

    @Override
    public void handle(Exception e) {
        if (e instanceof QuestionReadException) {
            log.error("Error reading questions from file: {}", e.getMessage());
        } else {
            log.error("An unexpected error occurred: {}", e.getMessage());
            log.debug("Exception details:", e);
        }
    }
}