package me.ailama.handler.commandhandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ailama.config.Config;
import me.ailama.main.Main;
import okhttp3.*;

import java.util.Base64;

public class Automatic1111Manager {

    private static Automatic1111Manager instance;

    private final String url;
    private final int steps;
    private final String scheduler;
    private final String samplerName;

    public Automatic1111Manager() {

        if (Config.get("AUTOMATIC1111_URL") == null || Config.get("AUTOMATIC1111_PORT") == null) {
            Main.LOGGER.warn("AUTOMATIC1111_URL or AUTOMATIC1111_PORT is not set in the config");

            url = null;
            steps = 0;
            scheduler = null;
            samplerName = null;
            return;
        }

        this.url = Config.get("AUTOMATIC1111_URL") + ":" + Config.get("AUTOMATIC1111_PORT") + "/sdapi/v1/txt2img";
        this.steps = Integer.parseInt(Config.get("AUTOMATIC1111_STEPS"));
        this.scheduler = Config.get("AUTOMATIC1111_SCHEDULER_TYPE").replace("'", "");
        this.samplerName = Config.get("AUTOMATIC1111_SAMPLER_NAME").replace("'", "");
    }

    public byte[] generateImage(String query) {

        try {
            OkHttpClient client = new OkHttpClient();

            JsonObject bodyObject = new JsonObject();

            bodyObject.addProperty("prompt", query);
            bodyObject.addProperty("steps", steps);
            bodyObject.addProperty("sampler_name", samplerName);
            bodyObject.addProperty("scheduler", scheduler);
            bodyObject.addProperty("cfg_scale", 7);

            // send a post request to the server
            RequestBody body = RequestBody.create(bodyObject.toString(), MediaType.parse("application/json"));

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            Response response = client.newBuilder()
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                    .newCall(request).execute();

            if (response.isSuccessful()) {
                // use gson to get images array and get the first element
                JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                JsonArray images = jsonObject.getAsJsonArray("images");
                return Base64.getDecoder().decode(images.get(0).getAsString().getBytes());
            }

            response.close();
        }
        catch (Exception e) {
            Main.LOGGER.error("Error while generating image: " + e.getMessage());
        }

        return null;
    }

    public boolean isAutomatic1111Enabled() {
        return url != null;
    }

    public static Automatic1111Manager getInstance() {
        if (instance == null) {
            instance = new Automatic1111Manager();
        }
        return instance;
    }

}
