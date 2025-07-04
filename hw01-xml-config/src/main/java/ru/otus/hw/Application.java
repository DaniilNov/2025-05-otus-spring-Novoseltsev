package ru.otus.hw;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.otus.hw.exceptions.ExceptionHandler;
import ru.otus.hw.service.TestRunnerService;

public class Application {
    public static void main(String[] args) {
        var context = new ClassPathXmlApplicationContext("spring-context.xml");
        var testRunnerService = context.getBean(TestRunnerService.class);
        var exceptionHandler = context.getBean(ExceptionHandler.class);

        try {
            testRunnerService.run();
        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }
}
