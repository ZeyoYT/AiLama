package me.ailama.handler.commandhandler;

import com.drew.lang.annotations.Nullable;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ailama.config.Config;
import me.ailama.handler.models.SearXNG;
import me.ailama.handler.models.SearXNGResult;
import me.ailama.main.AiLama;
import me.ailama.main.Main;
import okhttp3.OkHttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearXNGManager {

    private static SearXNGManager searXNGManager;

    private final String url;

    private final String finalUrl;

    // urls that either are blocked or fails to load as documents
    private final ArrayList<String> forbiddenUrls;

    private final String imageEngines = "google images,brave.images,bing images";

    private String engine;

    public SearXNGManager() {

        forbiddenUrls = new ArrayList<>();

        // If the URL or the Port is not set in the config, it will return null
        if(Config.get("SEARXNG_URL") == null || Config.get("SEARXNG_PORT") == null) {
            Main.LOGGER.warn("SEARXNG_URL or SEARXNG_PORT is not set in the config");

            url = null;
            finalUrl = null;
            return;
        }

        url = AiLama.getInstance().fixUrl(Config.get("SEARXNG_URL") + ":" + Config.get("SEARXNG_PORT"));
        engine = fixEngineString(Config.get("SEARXNG_ENGINES"));

        finalUrl = url + "/?q=%s&format=json&engines=";

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
        SearXNGResult bestResult = searXNG.results.getFirst();

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

                String searXUrl = String.format(finalUrl + engine, URLEncoder.encode(query, StandardCharsets.UTF_8));

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
    public List<String> getTopSearchResults(String query, int amount, boolean imageOnly) {
        if(url != null) {

            try {

                int amountToGet = Math.min(amount, 10);

                String finalEngine = imageOnly ? imageEngines : engine;

                String urlBuilder = finalUrl + finalEngine;

                String searXUrl = String.format(urlBuilder, URLEncoder.encode(query, StandardCharsets.UTF_8));

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

                List<SearXNGResult> filteredList = new ArrayList<>(searXNG.results.stream()
                        .filter(result -> !forbiddenUrls.contains(result.url))
                        .toList());

                // make it so that filteredList is sorted by score
                filteredList.sort((result1, result2) -> Double.compare(result2.score, result1.score));

                amountToGet = Math.min(amountToGet, filteredList.size());

                return filteredList.subList(0, amountToGet).stream()
                        .map(result -> {
                            if(imageOnly) {
                                return result.img_src;
                            }
                            return result.url;
                        })
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
        if(!forbiddenUrls.contains(url)) {
            forbiddenUrls.add(url);
            Main.LOGGER.warn("Added {} to the forbidden list: {}", url, reason);
        }
        else {
            Main.LOGGER.warn("{} was found in the forbidden list", url);
        }
    }

    public boolean isSearXNGEnabled() {
        return url != null;
    }

    public static SearXNGManager getInstance() {
        if (SearXNGManager.searXNGManager == null) {
            SearXNGManager.searXNGManager = new SearXNGManager();
        }
        return SearXNGManager.searXNGManager;
    }

    public boolean isForbiddenUrl(String url) {
        return forbiddenUrls.contains(url);
    }
}
