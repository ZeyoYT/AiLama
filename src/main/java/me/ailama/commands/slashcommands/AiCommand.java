package me.ailama.commands.slashcommands;

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

import java.util.*;
import java.util.regex.Pattern;

public class AiCommand implements AiLamaSlashCommand {

    public SlashCommandData getCommandData() {
        SlashCommandData slashCommandData = Commands.slash("ai", "ask ai (ollama)")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "ask", "The query you want to ask", true)
                .addOption(OptionType.STRING, "model", "Example (gemma:2b)", false)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)
                .addOption(OptionType.STRING, "url", "The Url of website for RAG", false)
                .addOption(OptionType.BOOLEAN, "reset-session", "If you want reset chat memory", false)

                .setNSFW(false);

        if(SearXNGManager.getInstance().isSearXNGEnabled()) {
            slashCommandData.addOption(OptionType.BOOLEAN, "web", "If you want the response based on web search", false);
        }

        return slashCommandData;
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

        // Set Configurations
        String modelOption = event.getOption("model") != null ? event.getOption("model").getAsString() : null;
        String queryOption = event.getOption("ask").getAsString();
        String urlOption = event.getOption("url") != null ? event.getOption("url").getAsString() : null;
        boolean resetSession = event.getOption("reset-session") != null && event.getOption("reset-session").getAsBoolean();

        String response = "";
        String sourceString = "";
        String urlForContent = null;


        String userId = event.getUser().getId();

        if (modelOption != null && !OllamaManager.getInstance().hasModel(modelOption)) {
            event.getHook().sendMessage("The model you provided is invalid, please provide a valid model").setEphemeral(true).queue();
            return;
        }

        if(resetSession) {
            OllamaManager.getInstance().getChatMemory(userId).clear();
        }

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

        boolean isTooledQuery = queryOption.startsWith(".");

        if(urlOption != null || urlForContent != null) {

            // if the url option is provided or the web option is provided, ask the assistant to answer the query based on the url
            Assistant assistant = OllamaManager.getInstance().urlAssistant( List.of(urlForContent != null ? urlForContent : urlOption) , modelOption, userId, isTooledQuery);

            if(assistant != null) {
                response = assistant.chat(userId,queryOption);
            }
        }
        else
        {
            // generate normal response based on the query if the url option is not provided or the web option is not provided
            if(isTooledQuery) {
                response = OllamaManager.getInstance().getTooledAssistant(modelOption, userId, null).answer(queryOption.replaceFirst(".", "") + "\n\n" + "user_id:" + userId);
            }
            else
            {
                response = OllamaManager.getInstance().
                        createAssistant(modelOption, userId)
                        .chat(userId,queryOption);
            }


            if(!isTooledQuery) {
                response = response.replace("_QUOTE_", "\"");
            }

        }

        // add the source of the content to the response
        if(response != null && !isTooledQuery && (urlOption != null || urlForContent != null)) {
            sourceString += "\n\nSource: <" + (urlForContent != null ? urlForContent : urlOption) + ">";
        }

        if(isTooledQuery) {
            ObjectMapper mapper = new ObjectMapper();

            String temp = Pattern.compile("(?<=\":\").*(?=\")").matcher(response).replaceAll(x -> x.group().replace("\"", "_QUOTE_") );

            try {
                Tool tooled = mapper.readValue(temp, Tool.class);

                if(!tooled.tooled) {
                    response = String.join("\n\n", tooled.response);
                }
                else
                {
                    if(OllamaManager.getInstance().isToolRawResponse(tooled.name)) {
                        response = OllamaManager.getInstance().executeTool(tooled.name, response).toString();
                    }
                    else
                    {
                        // if tooled.response contains a string that has ({response}) in it, replace it with the result of the tool
                        if(OllamaManager.getInstance().isToolResponseFormatted(tooled.name)) {

                            response = String.join("\n\n", tooled.response).replace("({response})", OllamaManager.getInstance().executeTool(tooled.name, tooled.parameters != null ? tooled.parameters.values().toArray() : List.of().toArray()).toString());

                        }
                        else
                        {
                            response = OllamaManager.getInstance().executeTool(tooled.name, tooled.parameters != null ? tooled.parameters.values().toArray() : List.of().toArray()).toString();
                        }
                    }
                }
            }
            catch (Exception e) {
                response = temp;
                Main.LOGGER.warn("Error while executing tool: {}", e.getMessage());
            }
        }

        String nullResponse = "I'm sorry, I don't understand what you're saying. did you provide the correct options?";

        if(response == null || response.isEmpty()) {
            response = nullResponse;
        }

        if(!response.equals(nullResponse) && !sourceString.isEmpty() && response.length() + sourceString.length() < 2000) {
            response += sourceString;
        }
        else if(response.length() > 2000) {

            List<String> responses = AiLama.getInstance().getParts(response, 2000);

            if(!sourceString.isEmpty()) {
                responses.add(sourceString);
            }

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
    }

}
