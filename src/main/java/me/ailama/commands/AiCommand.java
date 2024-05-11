package me.ailama.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.commandhandler.SearXNGManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import me.ailama.handler.interfaces.Assistant;
import me.ailama.handler.models.Tool;
import me.ailama.main.AiLama;
import me.ailama.main.Main;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;
import java.util.regex.Pattern;

public class AiCommand implements AiLamaSlashCommand {

    public SlashCommandData getCommandData() {
        return Commands.slash("ai","ask ai (ollama)")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "ask", "The query you want to ask", true)
                .addOption(OptionType.BOOLEAN, "web", "If you want the response based on web search", false)
                .addOption(OptionType.STRING, "model", "Example (gemma:2b)", false)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)
                .addOption(OptionType.STRING, "url", "The Url of website for RAG", false)

                .setNSFW(false);
    }

    public void handleCommand(SlashCommandInteractionEvent event) {

        int limitCount = 3;

        // Defer the reply to avoid timeout and set ephemeral if the option is provided
        if(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()) {
            event.deferReply(true).queue();
        }
        else {
            event.deferReply().queue();
        }

        AiLama.getInstance().startTimer();

        // Set Configurations
        String modelOption = event.getOption("model") != null ? event.getOption("model").getAsString() : null;
        String queryOption = event.getOption("ask").getAsString();
        String urlOption = event.getOption("url") != null ? event.getOption("url").getAsString() : null;

        String response = "";
        String urlForContent = null;

        // Check if the web option and url option are provided at the same time
        if(event.getOption("web") != null && event.getOption("web").getAsBoolean() && urlOption != null) {
            event.getHook().sendMessage("You can only provide one of web or url").setEphemeral(true).queue();
            return;
        }

        // if the web option is provided, get the url from the search
        if(event.getOption("web") != null && event.getOption("web").getAsBoolean()) {

            int tryCountForUrlSearch = 0;

            while (tryCountForUrlSearch < limitCount) {
                urlForContent = SearXNGManager.getInstance().getUrlFromSearch(queryOption);
                if(urlForContent != null) {
                    break;
                }
                tryCountForUrlSearch++;
            }

            if(urlForContent == null) {
                event.getHook().sendMessage("No proper results were found").setEphemeral(true).queue();
                return;
            }

        }

        if(urlOption != null || urlForContent != null) {
            // if the url option is provided or the web option is provided, ask the assistant to answer the query based on the url
            Assistant assistant = OllamaManager.getInstance().urlAssistant( List.of(urlForContent != null ? urlForContent : urlOption) , modelOption);

            if(assistant != null) {
                response = assistant.answer(queryOption);

                // add the source of the content to the response
                if(response != null) {
                    response += "\n\nSource: <" + (urlForContent != null ? urlForContent : urlOption) + ">";
                }
            }
        }
        else
        {
            // generate normal response based on the query if the url option is not provided or the web option is not provided
            response = OllamaManager.getInstance().
                    getTooledAssistant(modelOption)
                    .answer(queryOption);

            System.out.println(response);

            ObjectMapper mapper = new ObjectMapper();

            String temp = Pattern.compile("(?<=\":\").*(?=\")").matcher(response).replaceAll(x -> x.group().replace("\"", "_QUOTE_") );

            try {
                Tool tooled = mapper.readValue(temp, Tool.class);

                if(!tooled.tooled) {
                    response = String.join("\n\n", tooled.response);
                }
                else
                {
                    response = OllamaManager.getInstance().executeTool(tooled.name, tooled.arguments.values().toArray()).toString();
                }
            }
            catch (Exception e) {
                response = temp;
                Main.LOGGER.warn("Error while executing tool: " + e.getMessage());
            }

        }


        if(response == null || response.isEmpty()) {
            response = "I'm sorry, I don't understand what you're saying. did you provide the correct options?";
        }

        if(response.length() > 2000) {
            List<String> responses = AiLama.getInstance().getParts(response, 2000);

            for(String res : responses) {
                sendMessage(event, res);
            }

            return;
        }

        sendMessage(event, response);
    }

    public void sendMessage(SlashCommandInteractionEvent event, String response) {
        if(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()) {
            event.getHook().sendMessage(response).setEphemeral(true).queue();
        }
        else
        {
            event.getHook().sendMessage(response).queue();
        }

        System.out.println(AiLama.getInstance().getElapsedTime());
    }

}
