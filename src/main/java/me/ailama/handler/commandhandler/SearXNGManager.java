package me.ailama.handler.commandhandler;

import com.drew.lang.annotations.Nullable;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ailama.config.Config;
import me.ailama.handler.other.SearXNG;
import me.ailama.handler.other.SearXNGResult;
import me.ailama.main.AiLama;
import me.ailama.main.Main;
import okhttp3.OkHttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SearXNGManager {

    private static SearXNGManager searXNGManager;

    private final String url;

    private final String finalUrl;

    // urls that either are blocked or fails to load as documents
    private final ArrayList<String> forbiddenUrls;

    public SearXNGManager() {

        forbiddenUrls = new ArrayList<>();

        // If the URL or the Port is not set in the config, it will return null
        if(Config.get("SEARXNG_URL") == null || Config.get("SEARXNG_PORT") == null) {
            Main.LOGGER.error("SEARXNG_URL or SEARXNG_PORT is not set in the config");

            url = null;
            finalUrl = null;
            return;
        }

        url = AiLama.getInstance().fixUrl(Config.get("SEARXNG_URL") + ":" + Config.get("SEARXNG_PORT"));
        String engine = fixEngineString(Config.get("SEARXNG_ENGINES"));

        finalUrl = url + "/?q=%s&format=json&engines=" + engine;

    }

    /*
         - Makes the Engines String to be in the correct format
         - From "'[google, bing, duckduckgo]'" to "google,bing,duckduckgo"
         - it removes the spaces and the brackets
         - removes the quotes if multiple engines are provided
         - if the string is empty, it will be "none"
     */
    private static String fixEngineString(String engines) {
        if(engines.startsWith("'") && engines.endsWith("'")) {
            engines = engines.substring(1, engines.length() - 1);

            if(engines.isEmpty()) {
                engines = "duckduckgo";
            }

            if(engines.contains("[")) {
                engines = engines.replace("[", "").replace("]", "");
            }

            engines = engines.replace(" ", "");
        }
        return engines;
    }

    /*
        - Returns the best result from the SearXNG Results
        - The best result is the one with the highest score
        - If the URL is in the forbidden list, it will be removed
    */
    public SearXNGResult bestMatch(SearXNG searXNG) {
        SearXNGResult bestResult = searXNG.results.get(0);

        double bestMatchScore = bestResult.score;

        List<SearXNGResult> results = searXNG.results;
        results.removeIf(result -> forbiddenUrls.contains(result.url));

        for(SearXNGResult result : searXNG.results) {
            if(result.score > bestMatchScore) {
                bestResult = result;
                bestMatchScore = result.score;
            }
        }

        return bestResult;
    }

    /*
        - Returns the URL of the best result from the search
        - If no results are found, it will return null
    */
    @Nullable
    public String getUrlFromSearch(String query) {
        if(url != null) {

            try {

                String searXUrl = String.format(finalUrl, URLEncoder.encode(query, StandardCharsets.UTF_8));

                OkHttpClient client = new OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(searXUrl)
                        .build();

                String json = client.newCall(request).execute().body().string();

                ObjectMapper mapper = new ObjectMapper();
                SearXNG searXNG = mapper.readValue(json, SearXNG.class);

                if(searXNG.results.isEmpty()) {
                    return null;
                }

                if(!searXNG.results.get(0).toString().isEmpty()) {

                    SearXNGResult bestResult = bestMatch(searXNG);

                    return bestResult.url;

                }
            }
            catch (Exception e) {
                Main.LOGGER.error("Error while getting the URL from the search: " + e.getMessage());
                return null;
            }
        }

        return null;
    }

    /*
        - Returns X amount URLs with the best result from the search
        - If no results are found, it will return null

        - max amount is 10
        - min amount is 1
    */
    public List<String> getTopSearchResults(String query, int amount) {
        if(url != null) {

            try {

                int amountToGet = Math.min(amount, 10);
                String searXUrl = String.format(finalUrl, URLEncoder.encode(query, StandardCharsets.UTF_8));

                OkHttpClient client = new OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(searXUrl)
                        .build();

                String json = client.newCall(request).execute().body().string();

                ObjectMapper mapper = new ObjectMapper();
                SearXNG searXNG = mapper.readValue(json, SearXNG.class);

                if(searXNG.results.isEmpty()) {
                    return null;
                }

                ArrayList<SearXNGResult> bestResults = searXNG.results;

                List<SearXNGResult> filteredList = new ArrayList<>(searXNG.results.stream()
                        .filter(result -> !forbiddenUrls.contains(result.url))
                        .toList());

                // make it so that filteredList is sorted by score
                filteredList.sort((result1, result2) -> Double.compare(result2.score, result1.score));

                amountToGet = Math.min(amountToGet, filteredList.size());

                return filteredList.subList(0, amountToGet).stream()
                        .map(result -> result.url)
                        .toList();

            }
            catch (Exception e) {
                Main.LOGGER.error("Error while getting the URL from the search: " + e.getMessage());
                return null;
            }
        }

        return null;
    }

    public void addForbiddenUrl(String url, String reason) {
        forbiddenUrls.add(url);
        Main.LOGGER.info("Added " + url + " to the forbidden list" + (reason != null ? " because " + reason : ""));
    }

    public static SearXNGManager getInstance() {
        if (SearXNGManager.searXNGManager == null) {
            SearXNGManager.searXNGManager = new SearXNGManager();
        }
        return SearXNGManager.searXNGManager;
    }

}
