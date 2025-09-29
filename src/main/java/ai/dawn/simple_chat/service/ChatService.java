package ai.dawn.simple_chat.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;


@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, Advisor[] advisors) {
        this.chatClient = chatClientBuilder.defaultAdvisors(advisors).build();
    }

    public Flux<String> chat(Prompt prompt, String converationId) {
        return buildChatClientRequestSpec(prompt, converationId).stream().content();
    }

    private ChatClient.ChatClientRequestSpec buildChatClientRequestSpec(Prompt prompt, String converationId) {
        return chatClient.prompt(prompt).advisors(advisorSpec-> advisorSpec.param(ChatMemory.CONVERSATION_ID, converationId));
    }

    public ChatResponse call(Prompt prompt, String converationId) {
        return buildChatClientRequestSpec(prompt, converationId).call().chatResponse();
    }

    public enum Emotion {
        HAPPY,
        SAD,
        ANGRY,
        NEUTRAL
    }

    public record EmotionEvaluation(Emotion emotion, List<String> reasons) {}

    public EmotionEvaluation evaluateEmotion(Prompt prompt, String converationId) {
        return buildChatClientRequestSpec(prompt, converationId).call().entity(EmotionEvaluation.class);
    }
}
