package ru.otus.hw.exceptions;

public class ServiceTemporarilyUnavailableException extends RuntimeException {
    public ServiceTemporarilyUnavailableException(String message) {
        super(message);
    }
}