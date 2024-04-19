package me.ailama.autoevents.jdaevents;

import me.ailama.handler.commandhandler.EventManager;
import me.ailama.handler.interfaces.AiLamaEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModalEvent extends ListenerAdapter {
    public void onModalInteraction(ModalInteractionEvent event) {
        EventManager eventManager = EventManager.getEventManager();
        AiLamaEvent aiLamaEvent;

        if(eventManager.hasEvent(event.getUser().getId(), event.getModalId() + "_" + event.getGuild().getId())) {
            aiLamaEvent = eventManager.getEvent(event.getUser().getId(), event.getModalId() + "_" + event.getGuild().getId());
        }
        else if(eventManager.hasEvent(event.getUser().getId(), event.getModalId())) {
            aiLamaEvent = eventManager.getEvent(event.getUser().getId(), event.getModalId());
        }
        else if(eventManager.hasEvent(event.getGuild().getId() + "_guild", event.getModalId())) {
            aiLamaEvent = eventManager.getEvent(event.getGuild().getId() + "_guild", event.getModalId());
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

        aiLamaEvent.handleModalEvent(event);
        eventManager.removeEvent(event.getUser().getId(),event.getModalId());
    }
}
