package me.ailama.commands;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import me.ailama.config.Config;
import me.ailama.handler.other.Assistant;
import me.ailama.main.AiLama;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.stream.LangCollectors.joining;

public class AiCommand {

    public SlashCommandData getCommandData() {
        return Commands.slash("ai","ask ai (ollama)")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "ask", "The query you want to ask", true)
                .addOption(OptionType.STRING, "model", "Example (gemma:2b)", false)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)
                .addOption(OptionType.STRING, "url", "The Url of website for RAG", false)

                .setNSFW(false);
    }

    public void handleCommand(SlashCommandInteractionEvent event) {

        System.out.println(Config.get("WHITELISTED_USERS"));

        String whitelist = Config.get("WHITELISTED_USERS");
        whitelist = whitelist.substring(1, whitelist.length() - 1);
        String[] whitelistArray = whitelist.split(", ");

        List<String> whitelistedUsers = Arrays.asList(whitelistArray);

        if(!whitelistedUsers.contains(event.getUser().getId())) {
            event.reply("You are not allowed to use this command. its only made for " + event.getJDA().getUserById(Config.get("DEV_ID")).getAsMention() + "or a select few whitelisted people").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        String model = event.getOption("model") != null ? event.getOption("model").getAsString() : Config.get("OLLAMA_MODEL");
        String embModel = Config.get("OLLAMA_EMBEDDING_MODEL");
        String url = "http://" + Config.get("OLLAMA_URL") + ":" + Config.get("OLLAMA_PORT");
        String query = event.getOption("ask").getAsString();
        String response = "";
        boolean urlLoaded = false;


        if(event.getOption("url") != null) {

            try {
                URL webUrl = new URL(AiLama.getInstance().fixUrl(event.getOption("url").getAsString()));

                Document htmlDoc = UrlDocumentLoader.load(webUrl, new TextDocumentParser());
                HtmlTextExtractor transformer = new HtmlTextExtractor(null, null, true);
                Document document = transformer.transform(htmlDoc);

                Assistant assistant = createAssistant(document, model, embModel, url);
                response = assistant.answer(query);

                urlLoaded = true;
            }
            catch (Exception e) {
                event.getHook().sendMessage("Invalid URL").setEphemeral(true).queue();
                return;
            }

        }

        try {
            if(!urlLoaded) {
                OllamaChatModel.OllamaChatModelBuilder ollama = OllamaChatModel.builder().baseUrl(url).modelName(model).temperature(8.0);
                response = ollama.build().generate(query);
            }
        }
        catch (Exception e) {
            response = "I'm sorry, I'm having trouble processing your request. did you provide the correct model?";
        }

        if(response == null || response.isEmpty()) {
            response = "I'm sorry, I don't understand what you're saying.";
        }

        // if response is longer then 2000 characters, split it into multiple messages
        if(response.length() > 2000) {
            List<String> responses = AiLama.getInstance().getParts(response, 2000);
            for(String res : responses) {

                if(event.getOption("empherial") != null && event.getOption("ephemeral").getAsBoolean()) {
                    event.getHook().sendMessage(res).setEphemeral(true).queue();
                    continue;
                }

                event.getHook().sendMessage(res).queue();
            }
            return;
        }

        if(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()) {
            event.getHook().sendMessage(response).setEphemeral(true).queue();
            return;
        }

        event.getHook().sendMessage(response).queue();
    }

    public Assistant createAssistant(Document document, String model, String embModel, String url) {
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 0);
        List<TextSegment> segments = splitter.split(document);

        OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder().baseUrl(url).modelName(embModel).build();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
                .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
                .build();

        OllamaChatModel ollama = OllamaChatModel.builder().baseUrl(url).modelName(model).temperature(8.0).build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(ollama)
                .contentRetriever(contentRetriever)
                .build();
    }
}
