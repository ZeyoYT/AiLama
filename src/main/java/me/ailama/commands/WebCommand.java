package me.ailama.commands;

import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.commandhandler.SearXNGManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import me.ailama.handler.interfaces.Assistant;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public class WebCommand implements AiLamaSlashCommand {
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("web", "command that searches the web and generates a response based on the content")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "search", "The query you want to search for", true)
                .addOption(OptionType.STRING, "instructions", "additional instructions after getting search result", false)
                .addOption(OptionType.INTEGER, "limit", "The limit of search results", false)
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
        int limitOption = event.getOption("limit") != null ? event.getOption("limit").getAsInt() : 1;

        if(limitOption < 1) {
            event.getHook().sendMessage("Limit should be greater than 0").setEphemeral(true).queue();
            return;
        }

        if(limitOption > 10) {
            event.getHook().sendMessage("Limit should be less than equal to 10").setEphemeral(true).queue();
            return;
        }

        // Get the URL for the content
        List<String> urlForContent = SearXNGManager.getInstance().getTopSearchResults(queryOption, limitOption);

        // If no proper results were found from the search
        if(urlForContent == null) {
            event.getHook().sendMessage("No proper results were found").setEphemeral(true).queue();
            return;
        }

        // Create a URL Assistant
        Assistant assistant = OllamaManager.getInstance().urlAssistant(urlForContent, modelOption);

        // if there was an error while creating the assistant
        if(assistant == null) {
            event.getHook().sendMessage("No proper results were found").setEphemeral(true).queue();
            return;
        }

        // Get the response
        String response = assistant.answer(instructionOption != null ? instructionOption : queryOption);
        event.getHook().sendMessage(response).setEphemeral(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()).queue();
    }
}
