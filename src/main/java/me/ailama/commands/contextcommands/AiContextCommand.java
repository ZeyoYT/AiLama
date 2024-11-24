package me.ailama.commands.contextcommands;

import me.ailama.handler.commandhandler.EventManager;
import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.enums.EventCategoryEnum;
import me.ailama.handler.interfaces.AiLamaEvent;
import me.ailama.handler.interfaces.AiLamaMessageContextCommand;
import me.ailama.handler.interfaces.Assistant;
import me.ailama.main.AiLama;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class AiContextCommand implements AiLamaMessageContextCommand, AiLamaEvent {

    @Override
    public CommandData getCommandData() {
        return Commands.message("ai")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
                .setNSFW(false);
    }

    @Override
    public void handleCommand(MessageContextInteractionEvent event) {

        if(event.getTarget().getContentRaw().isEmpty()) {
            event.getHook().sendMessage("Please provide a message to get response, no embeds, images or files").queue();
            return;
        }

        Modal m = Modal.create(event.getId(), "AI Formatter")
                .addComponents(
                        ActionRow.of(TextInput.create("ephemeral", "Ephemeral ( 1 for yes )", TextInputStyle.SHORT).setMaxLength(1).setRequired(true).build())
                )
                .build();

        EventManager.getEventManager().addEventWithData(event.getUser().getId(), m.getId(), EventCategoryEnum.ModalEvent, this, event.getTarget());

        event.replyModal(m).queue();
    }

    @Override
    public void handleModalEvent(ModalInteractionEvent event) {

        boolean ephemeral = event.getValue("ephemeral").getAsString().equals("1");

        event.deferReply(ephemeral).queue();

        Message messageData = (Message)(EventManager.getEventManager().getEventData(event.getUser().getId(), event.getModalId()));
        String message = messageData.getContentRaw();

        String SystemMessage = """
                
                You are a helpful ai assistant that will generate response for users query.
                
                - you may use markdown to format your messages.
                
                """;

        Assistant assistant = OllamaManager.getInstance().createAssistantX(null, false)
                .systemMessageProvider(o -> SystemMessage.formatted(message))
                .build();

        String resp = assistant.answer(message);

        AiLama.getInstance().getParts(resp, 2000).forEach(p -> event.getHook().sendMessage(p).queue());

    }
}
