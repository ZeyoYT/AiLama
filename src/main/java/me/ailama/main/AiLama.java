package me.ailama.main;

import me.ailama.handler.annotations.Tool;
import me.ailama.handler.annotations.Args;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;

import java.util.ArrayList;
import java.util.List;

public class AiLama
{
    private static AiLama INSTANCE;

    public AiLama() {

    }

    @Tool(name = "fixUrl", description = "Fix a URL if it doesn't start with 'http://' or 'https://'", arguments = {
            @Args(name = "url", Type = "string")
    })
    public String fixUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    public String getRates(String currency,String currency2) {
        final OkHttpClient okHttpClient = new OkHttpClient();
        String rate = "";

        final Request request = new Request.Builder().url("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/" + currency + ".json").build();

        try {
            final Response response = okHttpClient.newCall(request).execute();
            final ResponseBody responseBody = response.body();

            if (responseBody != null) {

                final DataObject dataObject = DataObject.fromJson(responseBody.string());
                rate = dataObject.getObject(currency).getString(currency2);

                responseBody.close();
            }

            response.close();
            return rate;
        }
        catch (Exception e) {
            Main.LOGGER.error("Error while getting currency rate: " + e.getMessage());
            return "0";
        }
    }

    public List<String> getParts(final String string, final int partitionSize) {
        final List<String> parts = new ArrayList<>();
        for (int len = string.length(), i = 0; i < len; i += partitionSize) {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }

    public static AiLama getInstance() {
        if (AiLama.INSTANCE == null) {
            AiLama.INSTANCE = new AiLama();
        }
        return AiLama.INSTANCE;
    }

}
