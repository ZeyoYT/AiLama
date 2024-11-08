package me.ailama.commands;

import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

public class ModelsCommand implements AiLamaSlashCommand {
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("models","get list of all models available to ollama")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .setNSFW(false);
    }

    @Override
    public void handleCommand(SlashCommandInteractionEvent event) {

        List<String> models = new ArrayList<>(OllamaManager.getInstance().getModels());


        List<MessageEmbed> embeds = new ArrayList<>();

        while (!models.isEmpty()) {
            EmbedBuilder embed = new EmbedBuilder();
            StringBuilder description = new StringBuilder();

            embed.setTitle("Models");
            description.append("Here are the models available to Ollama:\n\n");

            for (int i = 0; i < 20 && !models.isEmpty(); i++) {
                description.append(models.getFirst()).append("\n");
                models.removeFirst();
            }

            embed.setDescription(description.toString());

            embeds.add(embed.build());
        }

        event.replyEmbeds(embeds).setEphemeral(true).queue();
    }
}
