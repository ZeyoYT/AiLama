package me.ailama.autoevents.commandhandler;

import me.ailama.config.Config;
import me.ailama.handler.commandhandler.CommandRegister;
import me.ailama.main.AiLama;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class HandleCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        String whitelist = Config.get("WHITELISTED_USERS");
        whitelist = whitelist.substring(1, whitelist.length() - 1);
        String[] whitelistArray = whitelist.split(", ");

        ArrayList<String> whitelistedUsers = new ArrayList<>(Arrays.asList(whitelistArray));
        whitelistedUsers.add(Config.get("DEV_ID"));

        if(!whitelistedUsers.contains(event.getUser().getId())) {

            event.getJDA().retrieveUserById(Config.get("DEV_ID")).queue(user -> {
                event.reply("You are not allowed to use this command. its only made for " + user.getAsMention() + "or a select few whitelisted people").setEphemeral(true).queue();
            }, err -> {
                event.reply("You are not allowed to use this command. its only made for the developer or a select few whitelisted people").setEphemeral(true).queue();
            });

            return;
        }

        if(CommandRegister.getInstance().getCommandMap().containsKey(event.getInteraction().getName())) {
            Thread.ofVirtual().start(() -> {
                CommandRegister.getInstance().getCommand(event.getInteraction().getName()).handleCommand(event);
            });
        }
    }
}
