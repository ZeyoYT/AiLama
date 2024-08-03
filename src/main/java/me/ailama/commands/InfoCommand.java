package me.ailama.commands;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaModel;
import dev.langchain4j.model.ollama.OllamaModelDetails;
import dev.langchain4j.model.ollama.OllamaModels;
import me.ailama.config.Config;
import me.ailama.handler.commandhandler.Automatic1111Manager;
import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.commandhandler.SearXNGManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class InfoCommand implements AiLamaSlashCommand {
    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("info","get information related to ollama")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .setNSFW(false);
    }

    @Override
    public void handleCommand(SlashCommandInteractionEvent event) {

        event.deferReply().setEphemeral(true).queue();

        String ollamaUrl = OllamaManager.getInstance().getUrl();
        String ollamaPort = String.valueOf(OllamaManager.getInstance().getPort());

        String isOllamaServerRunning = OllamaManager.getInstance().checkOllamaServer() ? "Yes" : "No";
        String currentModel = OllamaManager.getInstance().getCurrentModel();
        String defaultModel = Config.get("OLLAMA_MODEL");

        String hasChatMemory = OllamaManager.getInstance().hasMemoryEnabled() ? "Yes" : "No";
        int chatMemorySize = OllamaManager.getInstance().getChatMemorySize();

        String isWebSearchEnabled = SearXNGManager.getInstance().isSearXNGEnabled() ? "Yes" : "No";
        String isImageGenerationEnabled = Automatic1111Manager.getInstance().isAutomatic1111Enabled() ? "Yes" : "No";

        /*
        * Url: URL
        * Port: PORT
        *
        * Ollama Server Running: Yes/No
        * Current Model: MODEL
        * Default Model: MODEL
        * */

        String ollamaInformationBuilder = "Url: " + ollamaUrl + "\n" +
                "Port: " + ollamaPort + "\n\n" +
                "**Ollama Server Running :-** \n" + isOllamaServerRunning + "\n\n" +
                "**Current Model :-**\n" + currentModel + "\n\n" +
                "**Default Model :-**\n" + defaultModel;

        String chatMemoryBuilder = "Chat Memory :- \n" + hasChatMemory + "\n" +
                "Chat Memory Size :- \n" + chatMemorySize;

        String dependentCommandsEnabled = "Web Search :- \n" + isWebSearchEnabled + "\n" +
                "Image Generation :- \n" + isImageGenerationEnabled;


        EmbedBuilder embed = new EmbedBuilder()
                .setTitle( event.getJDA().getSelfUser().getEffectiveName() + " Connection & Data Information :- ")
                .setDescription("Information related to Ollama, Chat Memory, and Dependent Commands")
                .addBlankField(false)
                .addField("Ollama Information :-", ollamaInformationBuilder,true)
                .addBlankField(false)
                .addField("Chat Memory Information :- ", chatMemoryBuilder,true)
                .addField("Dependent Commands Enabled :- ", dependentCommandsEnabled,true)
                .setColor(0x001440);

        event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();

    }
}
