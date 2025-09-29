package ai.dawn.simple_chat.config;

import java.util.Optional;   
import java.util.Scanner;

import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ai.dawn.simple_chat.service.RagChatService;
import ch.qos.logback.classic.LoggerContext;

@Configuration
public class SimpleChatConfig {

    @Bean
    public SimpleLoggerAdvisor simpleLoggerAdvice() {
        return SimpleLoggerAdvisor.builder().build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(10).build();
    }

    @Bean 
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @ConditionalOnProperty(prefix = "app.cli", name = "enabled", havingValue = "true")
    @Bean
    public CommandLineRunner commandLineRunner(
        RagChatService ragChatService, 
        @Value("${app.cli.filter-expression:}") String filterExpression) 
        {
        return args -> {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.getLogger("ROOT").detachAppender("CONSOLE");

            try(Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("USER: ");
                    String message = scanner.nextLine();

                    ragChatService.chat(Prompt.builder().content(message).build(), "cli", Optional.ofNullable(filterExpression).filter(String::isBlank))
                    .doFirst(() -> System.out.print("ASSSITANTS: "))
                    .doOnNext(System.out::print)
                    .doOnComplete(System.out::println)
                    .blockLast();
                }
            }
        };
    }

}
