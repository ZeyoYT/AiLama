package me.ailama.commands;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;
import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.commandhandler.SearXNGManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import me.ailama.handler.interfaces.Assistant;
import me.ailama.main.AiLama;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public class AiCommand implements AiLamaSlashCommand {

    public SlashCommandData getCommandData() {
        return Commands.slash("ai","ask ai (ollama)")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "ask", "The query you want to ask", true)
                .addOption(OptionType.BOOLEAN, "web", "If you want the response based on web search", false)
                .addOption(OptionType.STRING, "model", "Example (gemma:2b)", false)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)
                .addOption(OptionType.STRING, "url", "The Url of website for RAG", false)

                .setNSFW(false);
    }

    public void handleCommand(SlashCommandInteractionEvent event) {

        // Defer the reply to avoid timeout and set ephemeral if the option is provided
        if(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()) {
            event.deferReply(true).queue();
        }
        else {
            event.deferReply().queue();
        }

        // Set Configurations
        String modelOption = event.getOption("model") != null ? event.getOption("model").getAsString() : null;
        String queryOption = event.getOption("ask").getAsString();
        String urlOption = event.getOption("url") != null ? event.getOption("url").getAsString() : null;

        String response = "";
        String urlForContent = null;

        // Check if the web option and url option are provided at the same time
        if(event.getOption("web") != null && event.getOption("web").getAsBoolean() && urlOption != null) {
            event.getHook().sendMessage("You can only provide one of web or url").setEphemeral(true).queue();
            return;
        }

        // if the web option is provided, get the url from the search
        if(event.getOption("web") != null && event.getOption("web").getAsBoolean()) {

            urlForContent = SearXNGManager.getInstance().getUrlFromSearch(queryOption);

            if(urlForContent == null) {
                event.getHook().sendMessage("No proper results were found").setEphemeral(true).queue();
                return;
            }

        }

        if(urlOption != null || urlForContent != null) {
            // if the url option is provided or the web option is provided, ask the assistant to answer the query based on the url
            Assistant assistant = urlAssistant(urlForContent, urlOption, modelOption);

            if(assistant != null) {
                response = Optional.ofNullable(assistant.answer(queryOption)).orElse("Web search failed");
            }
        }
        else
        {
            // generate normal response based on the query if the url option is not provided or the web option is not provided
            response = OllamaManager.getInstance().createAssistant(modelOption).answer(queryOption);
        }


        if(response == null || response.isEmpty()) {
            response = "I'm sorry, I don't understand what you're saying. did you provide the correct options?";
        }

        if(response.length() > 2000) {
            List<String> responses = AiLama.getInstance().getParts(response, 2000);

            for(String res : responses) {
                sendMessage(event, res);
            }

            return;
        }

        sendMessage(event, response);
    }

    public void sendMessage(SlashCommandInteractionEvent event, String response) {
        if(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()) {
            event.getHook().sendMessage(response).setEphemeral(true).queue();
        }
        else
        {
            event.getHook().sendMessage(response).queue();
        }
    }

    public Assistant urlAssistant(String urlForContent, String url, String model) {
        try {
            URL webUrl = null;

            if(urlForContent != null) {
                webUrl = new URL(urlForContent);
            }
            else {
                webUrl = new URL(AiLama.getInstance().fixUrl(url));
            }

            Document htmlDoc = UrlDocumentLoader.load(webUrl, new TextDocumentParser());
            HtmlTextExtractor transformer = new HtmlTextExtractor(null, null, true);
            Document document = transformer.transform(htmlDoc);

            return OllamaManager.getInstance().createAssistant(document, model);
        }
        catch (Exception e) {

            SearXNGManager.getInstance().addForbiddenUrl(urlForContent, e.getMessage());

            return null;
        }
    }


}
