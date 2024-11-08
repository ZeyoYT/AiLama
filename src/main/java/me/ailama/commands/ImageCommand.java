package me.ailama.commands;

import com.google.gson.Gson;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import me.ailama.config.Config;
import me.ailama.handler.JsonBuilder.JsonArray;
import me.ailama.handler.JsonBuilder.JsonObject;
import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.handler.interfaces.AiLamaSlashCommand;
import me.ailama.main.AiLama;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import okhttp3.*;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Set;

public class ImageCommand implements AiLamaSlashCommand {

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("image","Give your image to a vision model")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .setContexts(InteractionContextType.ALL)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)

                .addOption(OptionType.STRING, "prompt", "The query you want to ask", true)
                .addOption(OptionType.ATTACHMENT, "image", "The image you want to give to the model", true)
                .addOption(OptionType.BOOLEAN, "ephemeral", "If you want the response to be ephemeral", false)
                .addOption(OptionType.STRING, "model", "Example (llama3.2-vision:11b)", false)

                .setNSFW(false);
    }

    @Override
    public void handleCommand(SlashCommandInteractionEvent event) {

        // Defer the reply to avoid timeout and set ephemeral if the option is provided
        if(event.getOption("ephemeral") != null && event.getOption("ephemeral").getAsBoolean()) {
            event.deferReply(true).queue();
        }
        else {
            event.deferReply().queue();
        }

        Set<String> supportedDocumentType = Set.of(
                "png", "jpg", "jpeg", "webp"
        );

        // Set Configurations
        String prompt = event.getOption("prompt").getAsString();
        Message.Attachment image = event.getOption("image").getAsAttachment();

        String imageType = image.getFileExtension();

        if(!supportedDocumentType.contains(imageType)) {
            event.getHook().sendMessage("The document type is not supported").queue();
            return;
        }

        OllamaManager ollama = OllamaManager.getInstance();

        String model = event.getOption("model") != null ? event.getOption("model").getAsString() : ollama.getCurrentModel();

        if(!ollama.hasModel(model)) {
            event.getHook().sendMessage("The model is not available").queue();
            return;
        }

        if(!ollama.checkModelIsVision(model)) {
            event.getHook().sendMessage("The current model is not a vision model").queue();
            return;
        }

        if(!image.isImage()) {
            event.getHook().sendMessage("The provided document is not an image").queue();
            return;
        }

        try {
            InputStream imageStream = image.getProxy().download().join();
            byte[] imageBytes = imageStream.readAllBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String url = Config.get("OLLAMA_URL") + ":" + Config.get("OLLAMA_PORT") + "/api/chat";

            JsonObject mainObj = new JsonObject();

            mainObj.add("model", model)
                    .add("messages", new JsonArray().objects(
                            new JsonObject()
                                    .add("role", "user")
                                    .add("content", prompt)
                                    .add("images", new JsonArray().addString(
                                            base64Image
                                    ))
                    ));

            RequestBody postBody = RequestBody.create(mainObj.build(), MediaType.get("application/json"));

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(postBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        event.getHook().sendMessage("Error while processing the image: " + response.body().string()).queue();
                        return;
                    }

                    StringBuilder responseFromAi = new StringBuilder();

                    try (BufferedSource source = response.body().source()) {

                        // loop until streaming of json has ended
                        while (!source.exhausted()) {
                            String line = source.readUtf8Line();
                            if (line != null) {

                                String streamedContent = new Gson().fromJson(line, com.google.gson.JsonObject.class)
                                        .get("message").getAsJsonObject()
                                        .get("content").getAsString();

                                responseFromAi.append(streamedContent);
                            }
                        }

                        response.close();
                        AiLama.getInstance().getParts(responseFromAi.toString(), 2000).forEach(res -> event.getHook().sendMessage(res).queue());
                    }
                    catch (Exception e) {
                        event.getHook().sendMessage("Error while processing the image: " + e.getMessage()).queue();
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    event.getHook().sendMessage("Error while processing the image: " + e.getMessage()).queue();
                }
            });

        }
        catch (Exception e) {
            event.getHook().sendMessage("Error while processing the image: " + e.getMessage()).queue();
        }
    }
}
