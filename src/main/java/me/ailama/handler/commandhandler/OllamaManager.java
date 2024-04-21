package me.ailama.handler.commandhandler;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import me.ailama.config.Config;
import me.ailama.handler.interfaces.Assistant;
import me.ailama.main.AiLama;

import java.util.List;

public class OllamaManager {

    private static OllamaManager ollama;

    private final String url;
    private final String model;
    private final String embeddingModel;

    public OllamaManager() {

        url = AiLama.getInstance().fixUrl(Config.get("OLLAMA_URL") + ":" + Config.get("OLLAMA_PORT"));
        model = Config.get("OLLAMA_MODEL");
        embeddingModel = Config.get("OLLAMA_EMBEDDING_MODEL");

    }

    // Just a Simple Response
    public Assistant createAssistant(String modelName) {

        String aiModel = modelName != null ? modelName : model;

        OllamaChatModel ollama = OllamaChatModel.builder().baseUrl(url).modelName(aiModel).temperature(0.4).build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(ollama)
                .build();
    }

    // Response Based on Provided Document
    public Assistant createAssistant(Document document, String modelName) {

        String aiModel = modelName != null ? modelName : model;

        DocumentSplitter splitter = DocumentSplitters.recursive(500, 0);
        List<TextSegment> segments = splitter.split(document);

        OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder().baseUrl(url).modelName(this.embeddingModel).build();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.6)
                .build();

        OllamaChatModel ollama = OllamaChatModel.builder().baseUrl(url).modelName(aiModel).temperature(0.4).build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(ollama)
                .contentRetriever(contentRetriever)
                .build();
    }

    public static OllamaManager getInstance() {
        if (OllamaManager.ollama == null) {
            OllamaManager.ollama = new OllamaManager();
        }
        return OllamaManager.ollama;
    }

}
