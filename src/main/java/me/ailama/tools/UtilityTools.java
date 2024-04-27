package me.ailama.tools;

import me.ailama.handler.annotations.Args;
import me.ailama.handler.annotations.Tool;

import java.net.URI;

public class UtilityTools {
    @Tool(name = "isValidURL", description = "Check if a URL is valid like isValidURL(url)", arguments = {
            @Args(name = "url", Type = "string")
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
}
