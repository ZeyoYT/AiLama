package me.ailama.handler.interfaces;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface AiLamaSlashCommand {

    SlashCommandData getCommandData();

    void handleCommand(SlashCommandInteractionEvent event);

}
