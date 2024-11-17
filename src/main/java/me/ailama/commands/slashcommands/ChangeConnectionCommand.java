package me.ailama.commands.slashcommands;

import me.ailama.config.Config;
import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ChangeConnectionCommand implements AiLamaSlashCommand {
    public SlashCommandData getCommandData() {
        return Commands.slash("seturl","change the url of the ollama server")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "url", "url / ip of the server", true)
                .addOption(OptionType.INTEGER, "port", "Port of Ollama Server default is 11434", true)
                .addOption(OptionType.STRING, "model", "The model you want to switch to", false)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)

                .setNSFW(false);
    }

    @Override
    public void handleCommand(SlashCommandInteractionEvent event) {
        if(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()) {
            event.deferReply(true).queue();
        }
        else {
            event.deferReply().queue();
        }

        if(!event.getUser().getId().equals(Config.get("USER_ID"))) {
            event.getHook().sendMessage("You are not authorized to use this command").queue();
            return;
        }

        // Set Configurations
        String urlOption = event.getOption("url").getAsString();
        long portOption = event.getOption("port").getAsLong();

        if(!urlOption.startsWith("http://") || urlOption.startsWith("https://")) {
            event.getHook().sendMessage("Please provide the url with http or https").queue();
            return;
        }

        if(!OllamaManager.getInstance().setUrl(urlOption, portOption)) {
            event.getHook().sendMessage("Failed to change the url of the ollama server").queue();
            return;
        }

        String firstModel = OllamaManager.getInstance().getModels().getFirst();
        String model = event.getOption("model") != null ? event.getOption("model").getAsString() : firstModel;

        String message;

        if(OllamaManager.getInstance().hasModel(model)) {
            OllamaManager.getInstance().setModel(model, event.getUser().getId());
            message = "Model switched to " + model;
        }
        else {
            OllamaManager.getInstance().setModel(firstModel, event.getUser().getId());
            message = "Model switched to " + firstModel + "\nThe Model you provided is invalid, so the model has been set to first model found";
        }

        event.getHook().sendMessage("Successfully changed the url of the ollama server \nNote: Changes Made are only for this Session and will reset when the bot is restarted, for permanent changes, please change the config\n\n" + message ).queue();
    }
}
