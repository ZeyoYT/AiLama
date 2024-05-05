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
            String tools = String.format("tools = %s",OllamaManager.getInstance().getFinalJson().build());

            // generate normal response based on the query if the url option is not provided or the web option is not provided
            response = OllamaManager.getInstance().
                    createAssistantX(modelOption)
                    .systemMessageProvider(x ->
                        String.format("""
                            You are a helpful AI assistant, you have a score, which can either be good or bad,
                            you need to maintain a good score to be helpful, if you don't maintain a good score then you will be considered unhelpful.
                            
                            you will try to answer the users need as best as you can and only in JSON format, else you will be given a bad score.
                            
                            any of the tools description listed below match the specific needs of the query then use the tool to answer the query,
                            the tools description is as specific as possible, so don't assume that the tool can be used for anything else.
                            
                            finally if a tool is matched then give response using following schema:
                            
                            {
                                "tooled": true,
                                "name": "tool_name",
                                "arguments": {
                                    "argument_name": "value",
                                    ...
                                },
                                "reason": "detailed_reason_for_using_tool"
                            }
                            
                            the tool_name is the name of the tool that you are using, the arguments are the arguments that you are passing to the tool.
                            
                            following are the rules for tools:
                            if the tool description does not specify the user's needs then don't respond using a tool else you will be given a bad score.
                            if you don't pass the required arguments to the tool then you will be given a bad score.
                            if you pass a null value to a argument that specified NOT_NULL in its description then you will be given a bad score.
                            if you don't respect the arguments data type, you will be given a bad score.
                            if you don't respect the arguments description, you will be given a bad score.
                            
                            and if you don't follow the schema, you will be given a bad score, but if you follow the schema, you will be given a good score.
                            
                            if you don't find a tool that match the requirements of the user then respond to the user normally,
                            and also make the response to be encoded for the JSON format or you will be given a bad score,
                            and use the following schema:
                            
                            {
                                "tooled": false,
                                "response": [
                                    "paragraph",
                                    "paragraph",
                                    ...
                                ]
                            }
                            
                            in the above schema, the response is an array of paragraphs that you want to respond to the user, minimum of 1 paragraph.
                            each new paragraph should be a new string in the array.
                            between each paragraph, there should be '\\n'.
                            
                            %s
                            """,tools)
                    )
                    .build()
                    .answer(queryOption);

            ObjectMapper mapper = new ObjectMapper();

            String temp = Pattern.compile("(?<=\":\").*(?=\")").matcher(response).replaceAll(x -> x.group().replace("\"", "_QUOTE_") );

            try {
                Tool tooled = mapper.readValue(temp, Tool.class);

                if(!tooled.tooled) {
                    response = String.join("\n", tooled.response);
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
    }


}
