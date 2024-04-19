package me.ailama.autoevents.jdaevents;

import me.ailama.commands.AiCommand;
import me.ailama.main.Main;
import me.ailama.main.AiLama;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JDAReady extends ListenerAdapter {
    public void onReady(ReadyEvent event)
    {
        Logger LOGGER = Main.LOGGER;

        System.out.println();
        LOGGER.info("Bot Name : " + event.getJDA().getSelfUser().getAsTag());
        LOGGER.info("Bot ID : " + event.getJDA().getSelfUser().getId());
        LOGGER.info("Start Time : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")));
        System.out.println();
        LOGGER.info("Guild Found : " + event.getGuildTotalCount());
        LOGGER.info("Guild Available : " + event.getGuildAvailableCount());
        LOGGER.info("Guild UnAvailable : " + event.getGuildUnavailableCount());
        System.out.println();

        event.getJDA().updateCommands().addCommands(
                new AiCommand().getCommandData()
        ).queue();
    }
}
