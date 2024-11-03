package me.ailama.handler.commandhandler;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaModel;
import dev.langchain4j.model.ollama.OllamaModels;
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
import me.ailama.tools.*;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

import java.lang.reflect.Method;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OllamaManager {

    public static final double TEMPERATURE = 0.4;
    private static OllamaManager ollama;

    private String url;
    private String model;
    private final String embeddingModel;

    private final HashMap<String,Method> tools;
    private final HashMap<Class<?>, ArrayList<String>> classInstanceMap;

    private HashMap<String,Assistant> assistants;
    private HashMap<String, ChatMemory> chatMemories;
    private int chatMemoryLimit;

    private boolean isTooledAssistant = false;

    public OllamaManager() {

        url = AiLama.getInstance().fixUrl(Config.get("OLLAMA_URL") + ":" + Config.get("OLLAMA_PORT"));
        model = Config.get("OLLAMA_MODEL");
        embeddingModel = Config.get("OLLAMA_EMBEDDING_MODEL");
        chatMemoryLimit = Config.get("OLLAMA_CHAT_MEMORY_UPTO") == null ? 1 : Integer.parseInt(Config.get("OLLAMA_CHAT_MEMORY_UPTO"));

        tools = new HashMap<>();
        classInstanceMap = new HashMap<>();
        assistants = new HashMap<>();

        addTool(AiLama.getInstance());
        addTool(new MathTools());
        addTool(new ApiTools());
        addTool(new TimeTools());
        addTool(new UtilityTools());
    }

    /*
        Uses Reflection Library to add the tools to the HashMap with specific annotation
    */
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
            if(toolAnnotation.parameters().length > 0) {
                JsonArray parameters = new JsonArray();
                ArrayList<JsonObject> parametersJsonObjects = new ArrayList<>();

                JsonArray requiredParameters = new JsonArray();

                // Add the arguments to the JSON
                for (int i = 0; i < toolAnnotation.parameters().length; i++) {

                    JsonObject parameter = new JsonObject();

                    parameter.add("name",toolAnnotation.parameters()[i].name())
                            .add("type",toolAnnotation.parameters()[i].Type());

                    if(toolAnnotation.parameters()[i].description() != null && !toolAnnotation.parameters()[i].description().isEmpty()) {
                        parameter.add("description",toolAnnotation.parameters()[i].description());
                    }

                    if(toolAnnotation.parameters()[i].required()) {
                        requiredParameters.addString(toolAnnotation.parameters()[i].name());
                    }

                    parametersJsonObjects.add(parameter);
                }

                parameters.objects(parametersJsonObjects);
                object.add("parameters",parameters);
                object.add("required_parameters",requiredParameters);
            }

            // response formatter
            if(toolAnnotation.responseFormatter()) {

                StringBuilder responseFormatterVars = new StringBuilder();

                for(String s : toolAnnotation.returnVars()) {
                    responseFormatterVars.append("({").append(s).append("}), ");
                }

                object.add("response_formatter_variables", responseFormatterVars.substring(0, responseFormatterVars.toString().length() - 2));

            }

            // Add the tool object to the array
            toolJsonObjects.add(object);
        });

        return toolJsonArray.objects(toolJsonObjects);
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

    public boolean isToolRawResponse(String toolName) {
        return getTool(toolName) != null && getTool(toolName).getAnnotation(Tool.class).rawResponse();
    }

    public boolean isToolResponseFormatted(String toolName) {
        return getTool(toolName) != null && getTool(toolName).getAnnotation(Tool.class).responseFormatter();
    }

    // Execute the Tool
    public Object executeTool(String toolName, Object... args) {

        try {

            Method tool = getTool(toolName);

            if(tool == null) {
                return null;
            }

            int count = tool.getParameterCount();

            // pass null if the argument is not provided
            if(count != args.length) {
                Object[] newArgs = new Object[count];
                for (int i = 0; i < count; i++) {
                    if(i < args.length) {
                        newArgs[i] = args[i];
                    }
                    else {
                        newArgs[i] = null;
                    }
                }
                args = newArgs;
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

    public Assistant getAssistantFromID(String id) {

        if(assistants.containsKey(id)) {
            return assistants.get(id);
        }

        return null;
    }

    // todo fix model not changing for tooled calls
    public Assistant getTooledAssistant(String modelOption, String userID, List<Document> documents) {

        Assistant assistantFromID = getAssistantFromID(userID);

        if(assistantFromID != null && isTooledAssistant) {

            if(modelOption != null) {
                return getTooledAssistant(modelOption, userID, documents);
            }

            return assistantFromID;

        }

        DocumentSplitter splitter = DocumentSplitters.recursive(500, 0);
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder().baseUrl(url).modelName(this.embeddingModel).build();


       if(documents != null) {
           for (Document doc : documents) {
               List<TextSegment> segments = splitter.split(doc);
               List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

               embeddingStore.addAll(embeddings, segments);
           }
       }

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.6)
                .build();

        String tools = String.format("tools = %s",OllamaManager.getInstance().getFinalJson().build());

        assistantFromID = createAssistantX(modelOption)
                .systemMessageProvider(x ->
                        getFormat(tools)
                )
                .contentRetriever(contentRetriever)
                .build();

        // todo: possible bug here, as its global variable for all users
        isTooledAssistant = true;

        // Update or Add the Assistant to the HashMap
        assistants.put(userID, assistantFromID);

        return assistantFromID;
    }

    private static @NotNull String getFormat(String tools) {

        return String.format("""
                You are a helpful AI Assistant empowered to utilize tools to answer user queries.\s
                
                Tool Usage :-
                If a tool aligns with the user's query, employ it to generate a response using this format:
                
                {
                    "tooled": true,
                    "name": "tool_name",
                    "parameters": {
                        "argument_name": value,
                        ...
                    },
                    "response": [
                        "paragraph",
                        "paragraph",
                        ...
                    ],
                    "reason": "detailed_reason_for_using_tool",
                    "match_percentage": Number
                }
                
                Response Formatting :-
                - If a tool provides a response_formatter, utilize it to structure the tool's output. The formatter will include response_variables, and the response should resemble:
                
                "response": [
                    ".. text .. ({response_variable}) ... ({response_variable})",
                    ...
                ]
                - Unless explicitly specified otherwise by the user, format all responses using Markdown for better readability and structure.
                
                Tool Usage Rules :-
                - Responses using a tool must have tooled: true.
                - The name must correspond to a defined tool.
                - match_percentage must be >= 0.
                - reason must be provided, be non-empty, and not exceed 100 characters.
                - Use only parameters defined in the tool's JSON object.
                - Adhere strictly to the tool's description; don't assume broader capabilities.
                - If the tool doesn't fit the user's needs, don't use it.
                - Provide all required parameters; if missing, generate a value.
                - Use the provided user_id only for passing it as a parameter value.
                
                Parameter Rules :-
                - Adhere strictly to parameter descriptions; don't assume broader usage.
                - Parameter names must match those in the tool list.
                - Provide JSON values based on the defined parameter type.
                - Respect the order of parameters.
                - Don't create new parameters.
                
                Response Array Rules (with response_formatter_variables) :-
                - response_variable names must match those provided.
                - At least one paragraph is required in the response array.
                - Paragraphs must not exceed 50 words.
                - A \\\\n character must follow each paragraph.
                - All response_formatter_variables must be used.
                - When using them, enclose them in ({response_variable}).
                
                Response Formatter Usage :-
                	MUST use the provided response formatter to structure the tool's output.
                   	Example:-
                		User: "What is the time?"
                		Response: "The time is ({response})"
                		User: "What is my username and age?"
                		Response: "Your Username is: ({full_name}) and your age: ({age})"
                
                Non-Tool Responses :-
                
                If no tool matches, respond using this format:
                {
                     "tooled": false,
                     "response": [
                         "paragraph",
                         "paragraph",
                         ...
                     ],
                     "rule": "which_rule_used_to_not_use_tool"
                }
                
                Unhelpful Responses :-
                
                Any violation of these rules results in an unhelpful response.

                the available tools are listed below in json format :-
                
                %s
                """, tools);
    }

    public ChatMemory getChatMemory(String userId) {

        if(chatMemories == null) {
            chatMemories = new HashMap<>();
        }

        if(!chatMemories.containsKey(userId)) {
            chatMemories.put(userId, MessageWindowChatMemory.withMaxMessages(chatMemoryLimit));
        }

        return chatMemories.get(userId);
    }

    // Returns a custom Assistant that uses the provided model, allowing for more customization, has no memory
    public AiServices<Assistant> createAssistantX(String modelName) {

        String aiModel = modelName != null ? modelName : model;

        OllamaChatModel ollama = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(aiModel)
                .temperature(TEMPERATURE)
                .timeout(Duration.ofSeconds(Config.get("OLLAMA_TIMEOUT_SECONDS") == null ? 60 : Integer.parseInt(Config.get("OLLAMA_TIMEOUT_SECONDS"))))
                .format("json")
                .build();

        isTooledAssistant = false;

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(ollama);
    }

    // Just a Simple Response
    public Assistant createAssistant(String modelName, String userID) {

        Assistant assistant = getAssistantFromID(userID);

        if(assistant != null && !isTooledAssistant && modelName == null) {
            return assistant;
        }

        String aiModel = modelName != null ? modelName : model;

        OllamaChatModel ollama = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(aiModel)
                .temperature(TEMPERATURE)
                .timeout(Duration.ofSeconds(Config.get("OLLAMA_TIMEOUT_SECONDS") == null ? 60 : Integer.parseInt(Config.get("OLLAMA_TIMEOUT_SECONDS"))))
                .build();

        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(ollama)
                .chatMemoryProvider(memId -> getChatMemory(userID))
                .build();

        isTooledAssistant = false;

        // Update or Add the Assistant to the HashMap
        assistants.put(userID, assistant);

        return assistant;
    }

    // Response Based on Provided Document
    public Assistant createAssistant(List<Document> documents, String modelName, String systemMessage, String userId) {

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
                .timeout(Duration.ofSeconds(Config.get("OLLAMA_TIMEOUT_SECONDS") == null ? 60 : Integer.parseInt(Config.get("OLLAMA_TIMEOUT_SECONDS"))))
                .build();

        AiServices<Assistant> assistantAiServices = AiServices.builder(Assistant.class)
                .chatLanguageModel(ollama)
                .contentRetriever(contentRetriever);

        if(systemMessage != null) {
            assistantAiServices.systemMessageProvider(o -> systemMessage);
        }

        isTooledAssistant = false;

        return assistantAiServices
                .chatMemoryProvider(memId -> getChatMemory(userId))
                .build();
    }

    // Response Based on Provided URL
    public Assistant urlAssistant(List<String> url, String model, String userId, boolean isTooled) {

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

        if(isTooled) {
            return getTooledAssistant(model, userId, documents);
        }

        return OllamaManager.getInstance().createAssistant(documents, model, null, userId);

    }

    public void setModel(String model, String userId) {
        if(!hasModel(model)) {
            throw new IllegalArgumentException("Model does not exist");
        }

        assistants.remove(userId);
        createAssistant(model, userId);
        this.model = model;
    }

    public boolean hasModel(String model) {
        return getModels().contains(model);
    }

    public List<String> getModels() {
        OllamaModels models = new OllamaModels(url, Duration.ofSeconds(90), 3, false, false);
        return models.availableModels().content().stream().map(OllamaModel::getName).toList();
    }

    public boolean checkOllamaServer() {

        try {
            String fixedUrl = getUrl();

            InetAddress address = InetAddress.getByName(fixedUrl);
            int port = Integer.parseInt(AiLama.getInstance().fixUrl(url).split(":")[2]);

            Socket socket = new Socket(address, port);
            boolean state = socket.isConnected();

            // Close the socket connection after checking
            socket.close();

            return state;
        }
        catch (Exception e) {
            return false;
        }

    }

    public boolean setUrl(String newUrl, long port) {
        try {

            String hostname = URI.create(newUrl).getHost();
            InetAddress address = InetAddress.getByName(hostname);

            Socket socket = new Socket(address, (int) port);
            boolean state = socket.isConnected();

            if(state) {
                url = newUrl + ":" + port;
            }

            socket.close();
            return state && checkOllamaServer();
        }
        catch (Exception e) {
            return false;
        }
    }

    public String getUrl() {
        return AiLama.getInstance().fixUrl(url).split(":")[1].replaceAll("/", "");
    }

    public String getPort() {
        return AiLama.getInstance().fixUrl(url).split(":")[2];
    }

    public String getCurrentModel() {
        return model;
    }

    public boolean hasMemoryEnabled() {
        return chatMemoryLimit > 1;
    }

    public int getChatMemorySize() {
        return chatMemoryLimit;
    }

    public static OllamaManager getInstance() {
        if (OllamaManager.ollama == null) {
            OllamaManager.ollama = new OllamaManager();
        }
        return OllamaManager.ollama;
    }
}
