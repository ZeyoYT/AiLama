package me.ailama.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.ailama.handler.annotations.Parameter;
import me.ailama.handler.annotations.Tool;
import me.ailama.handler.commandhandler.OllamaManager;

import java.net.URI;

public class UtilityTools {
    @Tool(name = "isValidURL", description = "Check if a URL is valid like isValidURL(url)", parameters = {
            @Parameter(name = "url", Type = "string")
    })
    public boolean isValidURL(String url) {
        try {
            //noinspection ResultOfMethodCallIgnored
            new URI(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Tool(name = "toolsJson", description = "Get all the tools available to use")
    public String toolsJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(OllamaManager.getInstance().getFinalJson().build());

        return gson.toJson(jsonElement);
    }
}
