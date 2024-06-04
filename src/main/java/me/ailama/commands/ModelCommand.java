package me.ailama.commands;

import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ModelCommand implements AiLamaSlashCommand {
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("model","switch model for current session")
                .addOption(OptionType.STRING, "model_name", "The model you want to switch to", true);
    }

    @Override
    public void handleCommand(SlashCommandInteractionEvent event) {
        String model = event.getOption("model_name").getAsString();

        if(OllamaManager.getInstance().hasModel(model)) {
            OllamaManager.getInstance().setModel(model);

            event.reply("Model switched to " + model + "\nThe Model has only changed for this session, and on restart will reset to the one in Dot Env file").setEphemeral(true).queue();
            return;
        }

        StringBuilder models = new StringBuilder();

        OllamaManager.getInstance().getModels().forEach(v -> models.append(v).append("\n"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Invalid Model")
                .setDescription("The model you provided is invalid, please provide a valid model")
                .addField("Available Models", models.toString(), false);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
