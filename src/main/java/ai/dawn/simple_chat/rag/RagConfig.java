package ai.dawn.simple_chat.rag;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Configuration
public class RagConfig {

    @Bean
    public DocumentReader[] documentReaders(@Value("${app.rag.document-location-pattern}") String documentLocationPattern) throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(documentLocationPattern);
        return Arrays.stream(resources)
                .map(TikaDocumentReader::new)
                .toArray(DocumentReader[]::new);
    }

    @Bean
    public DocumentTransformer textSplitter() {
        return new LengthTextSplitter(500, 100);
    }

    @Bean
    public DocumentTransformer keywordMetadataEnricher(ChatModel chatModel) {
        return new KeywordMetadataEnricher(chatModel,4);
    }

    @Bean
    public DocumentWriter jsonConsoleDocumentWriter(ObjectMapper objectMapper) {
        return documents -> {
            System.out.println("=====[INFO]Writing documents to console ========");
            try {
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(documents));
            } catch (JsonProcessingException e) {
                System.err.println("=====[ERROR]Error writing documents to console ========");
                throw new RuntimeException(e);
            }

            System.out.println("=====[INFO]Documents written to console ========");

        };
    }

    @ConditionalOnProperty(prefix = "app.vector-store.in-memory", name = "enabled", havingValue = "true")
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @ConditionalOnProperty(prefix = "app.etl.pipleline", name = "init", havingValue = "true")
    @Order(1)
    @Bean
    public ApplicationRunner initEtlPipeline(DocumentReader[] documentReaders, 
                                           @Qualifier("textSplitter") DocumentTransformer textSplitter, 
                                           @Qualifier("keywordMetadataEnricher") DocumentTransformer keywordMetadataEnricher, 
                                           DocumentWriter[] documentWriters) {
        return args -> {
            Arrays.stream(documentReaders)
            .map(DocumentReader::read)
            .map(textSplitter)
            .map(keywordMetadataEnricher)
            .forEach(documents -> Arrays.stream(documentWriters)
                .forEach(documentWriter -> documentWriter.write(documents))
            );
        };
    }

    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
            VectorStore vectorStore, 
            ChatClient.Builder chatClientBuilder,
            Optional<DocumentPostProcessor> printDocumentProcessor
        ) {

        //final MultiQueryExpander multiQueryExpander = MultiQueryExpander.builder().chatClientBuilder(chatClientBuilder).build();
        final RetrievalAugmentationAdvisor.Builder documentRetrieverBuilder = RetrievalAugmentationAdvisor.builder()
        //.queryExpander(multiQueryExpander)
        //.queryTransformers(TranslationQueryTransformer.builder().chatClientBuilder(chatClientBuilder).targetLanguage("korean").build())
        .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
        .documentRetriever(
            VectorStoreDocumentRetriever.builder().vectorStore(vectorStore)
            .similarityThreshold(0.3)
            .topK(3).build()
        );
        printDocumentProcessor.ifPresent(documentRetrieverBuilder::documentPostProcessors);
        return documentRetrieverBuilder.build();
    }

    @ConditionalOnProperty(prefix = "app.cli", name = "enabled", havingValue = "true")
    @Bean
    public DocumentPostProcessor printDocumentProcessor() {
        return (query,documents) -> {
            System.out.println("=====[INFO]Printing documents ========");

            if (documents == null || documents.isEmpty()) {
                System.out.println("=====[INFO]No documents found ========");
                return documents;
            }
            
            AtomicInteger counter = new AtomicInteger(0);
            documents.forEach(document -> {
                int docNumber = counter.incrementAndGet();
                System.out.printf("%d Document, Score: %.2f%n", docNumber, document.getScore());
                System.out.println("================================================");
                Optional.ofNullable(document.getText()).stream().map(text -> text.split("\n")).flatMap(Arrays::stream).forEach(line -> System.out.printf("%s%n", line));
                System.out.println("================================================");
            });
            System.out.println("=====[INFO]Documents RAG printed ========");
            return documents;
        };
    }


}
