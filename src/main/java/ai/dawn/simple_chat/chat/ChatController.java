package ai.dawn.simple_chat.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.DefaultChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.dawn.simple_chat.service.ChatService;
import ai.dawn.simple_chat.service.ChatService.EmotionEvaluation;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    public record PromptBody(String conversationId, String userPrompt, String systemPrompt, DefaultChatOptions chatOptions) {}

    @PostMapping(value = "/call", produces = "application/json")
    public ChatResponse chat(@RequestBody PromptBody promptBody) {
        Prompt prompt = getPrompt(promptBody);
        return chatService.call(prompt, promptBody.conversationId());
    }

    private Prompt getPrompt(PromptBody promptBody) {
        List<Message> messages = new ArrayList<>();
        messages.add(UserMessage.builder().text(promptBody.userPrompt()).build());
        
        Optional.ofNullable(promptBody.systemPrompt()).filter(Predicate.not(String::isBlank))
        .map(SystemMessage.builder()::text)
        .map(SystemMessage.Builder::build)
        .ifPresent(messages::add);
        
        Prompt.Builder promptBuilder = Prompt.builder().messages(messages);
        Optional.ofNullable(promptBody.chatOptions()).ifPresent(promptBuilder::chatOptions);
        
        Prompt prompt = promptBuilder.build();
        return prompt;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody PromptBody promptBody) {
        Prompt prompt = getPrompt(promptBody);
        return chatService.chat(prompt, promptBody.conversationId());
    }

    @PostMapping(value = "/emotion", produces = "application/json")
    public EmotionEvaluation emotion(@RequestBody PromptBody promptBody) {
        Prompt prompt = getPrompt(promptBody);
        return chatService.evaluateEmotion(prompt, promptBody.conversationId());
    }


}
