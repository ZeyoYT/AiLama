package me.ailama.handler.interfaces;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface AiLamaMessageContextCommand {

    CommandData getCommandData();

    void handleCommand(MessageContextInteractionEvent event);


}

