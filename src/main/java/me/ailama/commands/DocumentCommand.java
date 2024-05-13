package me.ailama.commands;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import me.ailama.main.AiLama;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DocumentCommand implements AiLamaSlashCommand {
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("document", "command that generates a response based on the provided document")
                .setContexts(InteractionContextType.ALL)
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.ATTACHMENT, "document", "The document you want to provide", true)
                .addOption(OptionType.STRING, "instructions", "additional instructions after getting search result", false)
                .addOption(OptionType.STRING, "model", "Example (gemma:2b)", false)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)
                .addOption(OptionType.BOOLEAN, "reset-session", "If you want reset chat memory", false)

                .setNSFW(false);
    }

    @Override
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
        String instructionOption = event.getOption("instructions") != null ? event.getOption("instructions").getAsString() : null;
        boolean resetSession = event.getOption("reset-session") != null && event.getOption("reset-session").getAsBoolean();

        String userId = event.getUser().getId();

        // Get the document
        Message.Attachment document = event.getOption("document").getAsAttachment();

        if(resetSession) {
            OllamaManager.getInstance().getChatMemory(userId).clear();
        }

        // check if document provided is a text based document like .txt .docx .pdf etc
        if(!getSupportedExtensions().contains(document.getFileExtension())) {
            event.getHook().sendMessage("You can only provide text based documents like .txt, .docx, .pdf").setEphemeral(true).queue();
            return;
        }

        String response;

        // from bytes to document content
        try {
            Document docFromBytes = UrlDocumentLoader.load(document.getUrl(), new ApacheTikaDocumentParser());

            String systemMessage = "First give details about the document like keywords, number of characters, etc. then summarize it and also show the actual content without modification";

            // get the response from the document
            response = OllamaManager.getInstance().createAssistant(List.of(docFromBytes), modelOption, systemMessage, userId)
                    .chat(userId,instructionOption == null ? "Summarize it and also show the actual content without modification" : instructionOption);
        }
        catch (Exception e) {
            response = "Error while reading the document: " + e.getMessage();
        }

        // Send the response
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

    public List<String> getSupportedExtensions() {
        return List.of(
                "txt",
                "docx",
                "pdf"
        );
    }
}
