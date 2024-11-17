package me.ailama.autoevents.jdaevents;

import me.ailama.handler.commandhandler.CommandRegister;
import me.ailama.main.Main;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
                CommandRegister.getInstance().getAllCommandsData()
        ).queue();
    }
}
