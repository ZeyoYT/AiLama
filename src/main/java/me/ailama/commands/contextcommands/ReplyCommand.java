package me.ailama.commands.contextcommands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.langchain4j.spi.prompt.PromptTemplateFactory;
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
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.joda.time.DateTime;

import java.util.List;

public class ReplyCommand implements AiLamaMessageContextCommand, AiLamaEvent {

    @Override
    public CommandData getCommandData() {
        return Commands.message("reply")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
                .setNSFW(false);
    }

    @Override
    public void handleCommand(MessageContextInteractionEvent event) {

        Modal m = Modal.create(event.getId(), "Reply Formatter")
                .addComponents(
                        ActionRow.of(TextInput.create("reply_tone", "Reply Tone", TextInputStyle.SHORT).setRequired(true).build()),
                        ActionRow.of(TextInput.create("custom_system_message", "Custom System Message", TextInputStyle.PARAGRAPH).setRequired(false).build())
                )
                .build();

        EventManager.getEventManager().addEventWithData(event.getUser().getId(), m.getId(), EventCategoryEnum.ModalEvent, this, event.getTarget());

        event.replyModal(m).queue();
    }

    public void handleModalEvent(ModalInteractionEvent event) {

        event.deferReply().queue();

        String tone = event.getValue("reply_tone").getAsString();
        String customSystemMessage;

        if(event.getValue("custom_system_message") != null) {
            customSystemMessage = event.getValue("custom_system_message").getAsString();

            customSystemMessage += "\n\n" + """
                    As your knowledge base was cut off, you may use the following information in json for crafting the reply:
                    - %s
                    
                    User requirements for the reply:
                    - Tone : %s
                    
                    Craft the reply for the following message:
                    - %s
                    """;
        } else {
            customSystemMessage = null;
        }

        Message eventData = (Message) EventManager.getEventManager().getEventData(event.getUser().getId(), event.getModalId());

        String message = eventData.getContentRaw();
        String SystemMessage = """
                
                You will craft a reply for the message that user provides, but craft it in a way that it is a reply to the user's message.
                
                Rules :
                - don't enclose the message in quotes and make it copy paste ready.
                - you may use markdown to format your messages.
                - Reply should look like a reply to a friend.
                - use common slang and abbreviations.
                - must be short and concise.
                - don't use too many emojis.
                
                As your knowledge base was cut off, you may use the following information in json for crafting the reply:
                - %s
                
                User requirements for the reply:
                - Tone : %s
                
                Craft the reply for the following message:
                - %s
                
                """;

        String finalCustomSystemMessage = customSystemMessage;
        Assistant assistant = OllamaManager.getInstance().createAssistantX(null, false)
                .systemMessageProvider(o -> finalCustomSystemMessage != null ? String.format(finalCustomSystemMessage, getDateTime(), tone, message) : String.format(SystemMessage, getDateTime(), tone, message))
                .build();

        String resp = assistant.answer(message);

        AiLama.getInstance().getParts(resp, 2000).forEach(p -> event.getHook().sendMessage(p).queue());

    }

    // get date time
    public String getDateTime() {
        return DateTime.now().toDateTimeISO().toString();
    }
}
