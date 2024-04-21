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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jsoup.Jsoup;

import java.net.URL;

public class WebCommand implements AiLamaSlashCommand {
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("web", "command that searches the web and generates a response based on the content")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "search", "The query you want to search for", true)
                .addOption(OptionType.STRING, "instructions", "additional instructions after getting search result", false)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)
                .addOption(OptionType.STRING, "model", "Example (gemma:2b)", false)

                .setNSFW(false);
    }

    @Override
    public void handleCommand(SlashCommandInteractionEvent event) {

        // Defer the reply to avoid timeout and set ephemeral if the option is provided
        if (event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()) {
            event.deferReply(true).queue();
        } else {
            event.deferReply().queue();
        }

        // Set Configurations
        String queryOption = event.getOption("search").getAsString();
        String instructionOption = event.getOption("instructions") != null ? event.getOption("instructions").getAsString() : null;
        String modelOption = event.getOption("model") != null ? event.getOption("model").getAsString() : null;

        // Get the URL for the content
        String urlForContent = SearXNGManager.getInstance().getUrlFromSearch(queryOption);

        // If no proper results were found from the search
        if(urlForContent == null) {
            event.getHook().sendMessage("No proper results were found").setEphemeral(true).queue();
            return;
        }

        // Create an Assistant
        Assistant assistant = urlAssistant(urlForContent, null, modelOption);

        // if there was an error while creating the assistant
        if(assistant == null) {
            event.getHook().sendMessage("No proper results were found").setEphemeral(true).queue();
            return;
        }

        // Get the response
        String response = assistant.answer(instructionOption != null ? instructionOption : "give details on the content");
        event.getHook().sendMessage(response).setEphemeral(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()).queue();
    }

    // Response Based on Provided URL
    public Assistant urlAssistant(String urlForContent, String url, String model) {
        try {

            String webUrl = urlForContent != null ? urlForContent : AiLama.getInstance().fixUrl(url);
            String textOnly = Jsoup.connect(webUrl).get().body().text();
            Document document = Document.from(textOnly);

            return OllamaManager.getInstance().createAssistant(document, model);
        }
        catch (Exception e) {

            SearXNGManager.getInstance().addForbiddenUrl(urlForContent, e.getMessage());

            return null;
        }
    }
}
