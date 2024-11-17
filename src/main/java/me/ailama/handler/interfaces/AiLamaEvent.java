package me.ailama.handler.interfaces;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;

public interface AiLamaEvent {
    default void handleCustomEvent(Object event) {

    }

    default void handleModalEvent(ModalInteractionEvent event) {

    }

    default void handleButtonEvent(ButtonInteractionEvent event) {
        event.reply(event.getButton().getLabel() + " : Button Clicked").queue();
    }

    default void handleEntitySelectEvent(EntitySelectInteractionEvent event) {

    }
}
