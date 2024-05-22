package me.ailama.commands;

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
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.IOUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WebCommand implements AiLamaSlashCommand {
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("web", "command that searches the web and generates a response based on the content")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "search", "The query you want to search for", true)
                .addOption(OptionType.BOOLEAN, "for-image", "search for a image", false)
                .addOption(OptionType.STRING, "instructions", "additional instructions after getting search result", false)
                .addOption(OptionType.INTEGER, "limit", "The limit of search results", false)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)
                .addOption(OptionType.STRING, "model", "Example (gemma:2b)", false)
                .addOption(OptionType.BOOLEAN, "reset-session", "If you want reset chat memory", false)

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
        boolean resetSession = event.getOption("reset-session") != null && event.getOption("reset-session").getAsBoolean();
        boolean imageOnly = event.getOption("for-image") != null && event.getOption("for-image").getAsBoolean();

        int limitOption = event.getOption("limit") != null ? event.getOption("limit").getAsInt() : 1;

        String userId = event.getUser().getId();

        if(resetSession) {
            OllamaManager.getInstance().getChatMemory(userId).clear();
        }

        if(limitOption < 1) {
            event.getHook().sendMessage("Limit should be greater than 0").setEphemeral(true).queue();
            return;
        }

        if(limitOption > 10) {
            event.getHook().sendMessage("Limit should be less than equal to 10").setEphemeral(true).queue();
            return;
        }

        // Get the URL for the content
        List<String> urlForContent = SearXNGManager.getInstance().getTopSearchResults(queryOption, limitOption, imageOnly);

        // If no proper results were found from the search
        if(urlForContent == null) {
            event.getHook().sendMessage("No proper results were found").setEphemeral(true).queue();
            return;
        }

        if(imageOnly) {
            List<FileUpload> fileUploads = new ArrayList<>();

            for(String url : urlForContent) {
                try {
                    byte[] bytes = IOUtil.readFully(new URL(url).openStream());
                    fileUploads.add(FileUpload.fromData(bytes, "image.png"));
                } catch (Exception e) {
                    event.getHook().sendMessage("Error while sending image").setEphemeral(true).queue();
                    return;
                }
            }

            event.getHook().sendFiles(fileUploads).queue();
            return;
        }

        // Create a URL Assistant
        Assistant assistant = OllamaManager.getInstance().urlAssistant(urlForContent, modelOption, userId);

        // if there was an error while creating the assistant
        if(assistant == null) {
            event.getHook().sendMessage("No proper results were found").setEphemeral(true).queue();
            return;
        }

        // Get the response
        String response = assistant.chat(userId,instructionOption != null ? instructionOption : queryOption);
        response += "\n";

        // Add the source of the content
        StringBuilder source = new StringBuilder();
        boolean hadForbiddenUrl = false;

        for (String url : urlForContent) {
            if(SearXNGManager.getInstance().isForbiddenUrl(url)) {
                hadForbiddenUrl = true;
                continue;
            }

            source.append("\nSource: <").append(url).append(">");
        }

        if(hadForbiddenUrl) {
            source.append("\n\nSome URLs were forbidden and were not included in the source");
        }

        response += source.toString();

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
}
