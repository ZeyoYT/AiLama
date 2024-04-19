package me.ailama.autoevents.commandhandler;

import me.ailama.commands.AiCommand;
import me.ailama.config.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        String whitelist = Config.get("WHITELISTED_USERS");
        whitelist = whitelist.substring(1, whitelist.length() - 1);
        String[] whitelistArray = whitelist.split(", ");

        ArrayList<String> whitelistedUsers = new ArrayList<>(Arrays.asList(whitelistArray));
        whitelistedUsers.add(Config.get("DEV_ID"));

        if(!whitelistedUsers.contains(event.getUser().getId())) {
            event.reply("You are not allowed to use this command. its only made for " + event.getJDA().getUserById(Config.get("DEV_ID")).getAsMention() + "or a select few whitelisted people").setEphemeral(true).queue();
            return;
        }

        if(event.getInteraction().getName().equals("ai")) {
            new AiCommand().handleCommand(event);
        }
    }
}
