package me.ailama.autoevents.commandhandler;

import me.ailama.commands.AiCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HandleCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getInteraction().getName().equals("ai")) {
            new AiCommand().handleCommand(event);
        }
    }
}
