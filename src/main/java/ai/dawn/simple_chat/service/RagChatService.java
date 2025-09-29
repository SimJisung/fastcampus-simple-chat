package ai.dawn.simple_chat.service;

import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class RagChatService {

    private final ChatClient chatClient;

    public RagChatService(ChatClient.Builder chatClientBuilder, Advisor[] advisors) {
        this.chatClient = chatClientBuilder.defaultOptions(ChatOptions.builder().temperature(0.0).build()).defaultAdvisors(advisors).build();
    }

    public Flux<String> chat(Prompt prompt, String converationId, Optional<String> filterExpressionAsOpt) {
        return buildChatClientRequestSpec(prompt, converationId, filterExpressionAsOpt).stream().content();
    }

    private ChatClient.ChatClientRequestSpec buildChatClientRequestSpec(Prompt prompt, String converationId, Optional<String> filterExpressionAsOpt) {
        ChatClientRequestSpec advisors = chatClient.prompt(prompt).advisors(advisorSpec-> advisorSpec.param(ChatMemory.CONVERSATION_ID, converationId));
        filterExpressionAsOpt.ifPresent(filterExpression -> advisors.advisors(advisorSpec -> advisorSpec.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpression)));
        return advisors;
    }

    public ChatResponse call(Prompt prompt, String converationId, Optional<String> filterExpressionAsOpt) {
        return buildChatClientRequestSpec(prompt, converationId, filterExpressionAsOpt).call().chatResponse();
    }    

}
