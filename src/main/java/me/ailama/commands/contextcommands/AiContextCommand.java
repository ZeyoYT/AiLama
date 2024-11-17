package me.ailama.commands.contextcommands;

import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.interfaces.AiLamaMessageContextCommand;
import me.ailama.handler.interfaces.Assistant;
import me.ailama.main.AiLama;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class AiContextCommand implements AiLamaMessageContextCommand {

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
        event.deferReply().queue();

        String message = event.getTarget().getContentRaw();
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
