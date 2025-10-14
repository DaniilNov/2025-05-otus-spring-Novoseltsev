package ru.otus.hw.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.otus.hw.models.CoffeeOrder;
import ru.otus.hw.services.CoffeeProcessingService;
import ru.otus.hw.services.ErrorHandlingService;

import java.util.concurrent.Executor;

@Configuration
public class CoffeeIntegrationConfig {

    @Bean
    public Executor coffeeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Coffee-");
        executor.initialize();
        return executor;
    }

    @Bean
    public MessageChannel coffeeInputChannel() {
        return new ExecutorChannel(coffeeTaskExecutor());
    }


    @Bean
    public MessageChannel errorChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public IntegrationFlow errorHandlingFlow(ErrorHandlingService errorService) {
        return IntegrationFlow.from(errorChannel())
                .enrichHeaders(h -> h.header("error-timestamp", System.currentTimeMillis()))
                .handle(errorService::handleError)
                .get();
    }

    @Bean
    public IntegrationFlow coffeeProcessingFlow(CoffeeProcessingService service) {
        return IntegrationFlow.from(coffeeInputChannel())
                .<CoffeeOrder, String>route(CoffeeOrder::getType, mapping -> mapping
                        .subFlowMapping("espresso", subFlow -> subFlow
                                .transform(service::roastCoffee)
                                .transform(service::grindCoffee)
                                .transform(service::brewEspresso))
                        .subFlowMapping("cappuccino", subFlow -> subFlow
                                .transform(service::roastCoffee)
                                .transform(service::grindCoffee)
                                .transform(service::brewCappuccino))
                        .defaultOutputChannel(errorChannel()))
                .get();
    }
}