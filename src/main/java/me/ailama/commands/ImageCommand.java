package me.ailama.commands;

import com.google.gson.JsonParser;
import me.ailama.handler.commandhandler.Automatic1111Manager;
import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import me.ailama.handler.interfaces.Assistant;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

public class ImageCommand implements AiLamaSlashCommand {
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("image","generate a image (automatic1111)")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "prompt", "The query you want to ask", true)
                .addOption(OptionType.BOOLEAN, "improve_prompt", "If you want to improve the prompt using ai (slow response)", false)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)

                .setNSFW(false);
    }

    @Override
    public void handleCommand(SlashCommandInteractionEvent event) {
        if(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()) {
            event.deferReply(true).queue();
        }
        else {
            event.deferReply().queue();
        }

        // Set Configurations
        String queryOption = event.getOption("prompt").getAsString();

        if(event.getOption("improve_prompt") != null && event.getOption("improve_prompt").getAsBoolean()) {
            Assistant assistant = OllamaManager.getInstance().createAssistantX(null)
                    .systemMessageProvider((msg) -> """
                            You are a helpful AI assistant, you have a score, which can either be good or bad,
                            you need to maintain a good score to be helpful, if you don't maintain a good score then you will be considered unhelpful.
                            
                            The User would provide you with a prompt, that prompt would later be used to generate an image.
                            you need to improve the prompt to make it more understandable and clear and if you don't improve the prompt you would be given a bad score.
                            
                            the following guide should be followed else you would be given a extremely bad score:
                            1. The prompt should be clear and understandable
                            2. The prompt should be improved to make it more understandable
                            3. Don't change the meaning of the original prompt
                            4. The more you add parentheses the more focus would be on the keyword but it will sometimes reduce the quality of the image
                            
                            after you have improved the prompt, you will return only the improved prompt as a response or you would be given a bad score.
                            
                            use the following schema to respond:
                            {
                                "prompt": "The improved provided prompt"
                                "user_prompt": "The original prompt provided by the user"
                            }
                            
                            """)
                    .build();

            queryOption = JsonParser.parseString(assistant.answer(queryOption)).getAsJsonObject().get("prompt").getAsString();
        }

        byte[] bytes = Automatic1111Manager.getInstance().generateImage(queryOption);

        if(bytes == null) {
            event.getHook().sendMessage("Error while generating image").queue();
            return;
        }

        event.getHook().sendFiles(FileUpload.fromData(bytes, "image.png")).queue();

    }
}
