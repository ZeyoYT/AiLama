package me.ailama.autoevents.jdaevents;

import me.ailama.handler.commandhandler.EventManager;
import me.ailama.handler.interfaces.AiLamaEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonEvent extends ListenerAdapter {
    public void onButtonInteraction(ButtonInteractionEvent event) {
        EventManager eventManager = EventManager.getEventManager();

        AiLamaEvent aiLamaEvent;

        if(eventManager.hasEvent(event.getUser().getId(), event.getButton().getId() + "_" + event.getGuild().getId())) {
            aiLamaEvent = eventManager.getEvent(event.getUser().getId(), event.getButton().getId() + "_" + event.getGuild().getId());
        }
        else if(eventManager.hasEvent(event.getUser().getId(), event.getButton().getId())) {
            aiLamaEvent = eventManager.getEvent(event.getUser().getId(), event.getButton().getId());
        }
        else if(eventManager.hasEvent(event.getGuild().getId() + "_guild", event.getButton().getId())) {
            aiLamaEvent = eventManager.getEvent(event.getGuild().getId() + "_guild", event.getButton().getId());
        }
        else if(eventManager.getEvents().isEmpty()) {
            event.reply("This is an old event, please rerun the command").setEphemeral(true).queue();

            event.getMessage().getComponents().forEach(x -> {
                event.getMessage().editMessageComponents(x.asDisabled()).queue(s -> {}, e -> {});
            });

            return;
        }
        else {
            event.reply("You did not fire this command").setEphemeral(true).queue();
            return;
        }

        aiLamaEvent.handleButtonEvent(event);
        eventManager.removeEvent(event.getUser().getId(),event.getButton().getId());
    }
}
