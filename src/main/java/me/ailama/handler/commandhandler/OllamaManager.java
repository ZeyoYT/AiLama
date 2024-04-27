package me.ailama.handler.commandhandler;

import com.fasterxml.jackson.databind.util.JSONPObject;
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
import me.ailama.handler.JsonBuilder.JsonArray;
import me.ailama.handler.JsonBuilder.JsonObject;
import me.ailama.handler.annotations.Tool;
import me.ailama.handler.interfaces.Assistant;
import me.ailama.main.AiLama;
import me.ailama.main.Main;
import me.ailama.tools.ApiTools;
import me.ailama.tools.MathTools;
import me.ailama.tools.TimeTools;
import me.ailama.tools.UtilityTools;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OllamaManager {

    public static final double TEMPERATURE = 0.4;
    private static OllamaManager ollama;

    private final String url;
    private final String model;
    private final String embeddingModel;

    private final HashMap<String,Method> tools;

    private final HashMap<Class<?>, ArrayList<String>> classInstanceMap;

    public OllamaManager() {

        url = AiLama.getInstance().fixUrl(Config.get("OLLAMA_URL") + ":" + Config.get("OLLAMA_PORT"));
        model = Config.get("OLLAMA_MODEL");
        embeddingModel = Config.get("OLLAMA_EMBEDDING_MODEL");

        tools = new HashMap<>();
        classInstanceMap = new HashMap<>();

        addTool(AiLama.getInstance());
        addTool(new MathTools());
        addTool(new ApiTools());
        addTool(new TimeTools());
        addTool(new UtilityTools());
    }

    public void addTool(Object toolClass) {

        ArrayList<String> toolsList = new ArrayList<>();

        getMethodsAnnotated(toolClass.getClass()).forEach(method -> {

            if(method.getReturnType() == void.class) {
                throw new IllegalArgumentException("Tool method must have a return type");
            }

            try {
                if(method.isAnnotationPresent(Tool.class)) {

                    String name = method.getAnnotation(Tool.class).name();

                    if(tools.containsKey(name)) {
                        throw new IllegalArgumentException("Tool name already exists");
                    }

                    tools.put(name,method);
                    toolsList.add(name);
                }
            } catch (Exception e) {
                Main.LOGGER.error("Error while adding tool: " + e.getMessage());
            }
        });

        if(!toolsList.isEmpty()) {
            classInstanceMap.put(toolClass.getClass(), toolsList);
        }
    }

    // Get the final JSON of all the tools
    public JsonArray getFinalJson() {
        JsonArray toolJsonArray = new JsonArray();

        List<JsonObject> toolJsonObjects = new ArrayList<>();

        tools.forEach((name, tool) -> {
            Tool toolAnnotation = tool.getAnnotation(Tool.class);

            JsonObject object = new JsonObject()
                    .add("name",toolAnnotation.name())
                    .add("description",toolAnnotation.description());

            // Add Arguments if there are any
            if(toolAnnotation.arguments().length > 0) {
                JsonArray arguments = new JsonArray();
                ArrayList<JsonObject> argumentJsonObjects = new ArrayList<>();

                // Add the arguments to the JSON
                for (int i = 0; i < toolAnnotation.arguments().length; i++) {

                    JsonObject argument = new JsonObject();

                    argument.add("name",toolAnnotation.arguments()[i].name())
                            .add("type",toolAnnotation.arguments()[i].Type());

                    if(!toolAnnotation.arguments()[i].description().isEmpty()) {

                        StringBuilder description = getStringBuilder(toolAnnotation, i);

                        argument.add("description",description.toString().trim());
                    }

                    argumentJsonObjects.add(argument);
                }

                arguments.objects(argumentJsonObjects);
                object.add("arguments",arguments);
            }

            // Add the tool object to the array
            toolJsonObjects.add(object);
        });


        return toolJsonArray.objects(toolJsonObjects);
    }

    @NotNull
    private static StringBuilder getStringBuilder(Tool toolAnnotation, int i) {
        StringBuilder description = new StringBuilder();
        description.append(toolAnnotation.arguments()[i].description());

        // if No Null
        if(toolAnnotation.arguments()[i].noNull()) {
            description.append(" NOT_NULL ");
        }

        // if Required
        if(toolAnnotation.arguments()[i].required()) {
            description.append(" REQUIRED ");
        }

        return description;
    }

    // Get the Tool Method
    public Method getTool(String toolName) {
        return tools.get(toolName);
    }

    public Class<?> getToolClass(String toolName) {
        for(Class<?> clazz : classInstanceMap.keySet()) {
            if(classInstanceMap.get(clazz).contains(toolName)) {
                return clazz;
            }
        }
        return null;
    }

    // Execute the Tool
    public Object executeTool(String toolName, Object... args) {

        try {

            Method tool = getTool(toolName);

            if(tool == null) {
                return null;
            }

            return tool.invoke(getToolClass(toolName).getConstructor().newInstance(), args);

        } catch (Exception e) {
            Main.LOGGER.error("Error while executing tool: " + e.getMessage());
            return null;
        }
    }

    // Get all methods annotated with Tool
    public static List<Method> getMethodsAnnotated(final Class<?> type) {
        final List<Method> methods = new ArrayList<>();
        Class<?> klass = type;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            for (final Method method : klass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Tool.class)) {
                    methods.add(method);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
        return methods;
    }

    // Returns a custom Assistant that uses the provided model, allowing for more customization
    public AiServices<Assistant> createAssistantX(String modelName) {

        String aiModel = modelName != null ? modelName : model;

        OllamaChatModel ollama = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(aiModel)
                .temperature(TEMPERATURE)
                .format("json")
                .build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(ollama);
    }

    // Just a Simple Response
    public Assistant createAssistant(String modelName) {

        String aiModel = modelName != null ? modelName : model;

        OllamaChatModel ollama = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(aiModel)
                .temperature(TEMPERATURE)
                .build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(ollama)
                .build();
    }

    // Response Based on Provided Document
    public Assistant createAssistant(List<Document> documents, String modelName, String systemMessage) {

        String aiModel = modelName != null ? modelName : model;

        DocumentSplitter splitter = DocumentSplitters.recursive(500, 0);
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder().baseUrl(url).modelName(this.embeddingModel).build();


        for (Document doc : documents) {
            List<TextSegment> segments = splitter.split(doc);
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            embeddingStore.addAll(embeddings, segments);
        }

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.6)
                .build();

        OllamaChatModel ollama = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(aiModel)
                .temperature(TEMPERATURE)
                .build();

        AiServices<Assistant> assistantAiServices = AiServices.builder(Assistant.class)
                .chatLanguageModel(ollama)
                .contentRetriever(contentRetriever);

        if(systemMessage != null) {
            assistantAiServices.systemMessageProvider(o -> systemMessage);
        }

        return assistantAiServices
                .build();
    }

    // Response Based on Provided URL
    public Assistant urlAssistant(List<String> url, String model) {

        List<Document> documents = new ArrayList<>();

        for (String u : url) {

            try {

                String webUrl = AiLama.getInstance().fixUrl(u);
                String textOnly = Jsoup.connect(webUrl).get().body().text();
                Document document = Document.from(textOnly);

                documents.add(document);

            }
            catch (Exception e) {

                SearXNGManager.getInstance().addForbiddenUrl(u, e.getMessage());

            }
        }

        if(documents.isEmpty()) {
            return null;
        }

        return OllamaManager.getInstance().createAssistant(documents, model, null);

    }

    public static OllamaManager getInstance() {
        if (OllamaManager.ollama == null) {
            OllamaManager.ollama = new OllamaManager();
        }
        return OllamaManager.ollama;
    }

}
