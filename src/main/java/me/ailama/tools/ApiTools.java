package me.ailama.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.ailama.handler.annotations.Parameter;
import me.ailama.handler.annotations.Tool;
import me.ailama.handler.commandhandler.OllamaManager;
import me.ailama.main.Main;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;

public class ApiTools {
    @Tool(name = "currencyRate", description = "Converts a currency rate to another", parameters = {
            @Parameter(name = "amount", Type = "double", description = "Amount to convert"),
            @Parameter(name = "currency1", Type = "string", description = "Currency to convert from, Like INR"),
            @Parameter(name = "currency2", Type = "string", description = "Currency to convert to, Like USD")
    }, responseFormatter = true)
    public String currencyRate(Double amount, String currency1, String currency2) {
        final double finalRate = Double.parseDouble(getRates(currency1.toLowerCase(),currency2.toLowerCase()));
        final double conv = finalRate * amount;
        return String.format("%.4f", conv);
    }

    public String getRates(String currency,String currency2) {
        final OkHttpClient okHttpClient = new OkHttpClient();
        String rate = "";

        final Request request = new Request.Builder().url("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/" + currency + ".json").build();

        try {
            final Response response = okHttpClient.newCall(request).execute();
            final ResponseBody responseBody = response.body();

            final DataObject dataObject = DataObject.fromJson(responseBody.string());
            rate = dataObject.getObject(currency).getString(currency2);

            responseBody.close();

            response.close();
            return rate;
        }
        catch (Exception e) {
            Main.LOGGER.error("Error while getting currency rate: " + e.getMessage());
            return "0";
        }
    }

    @Tool(name = "callApi", description = "used for calling restful api", parameters = {
            @Parameter(name = "api_url", Type = "string", description = "full api url, like https://api.example.com"),
            @Parameter(name = "headers", Type = "json_object", description = "headers to pass for the api call"),
            @Parameter(name = "call_type", Type = "STRING{GET,POST}", description = "define the api call type"),
            @Parameter(name = "body", Type = "string", description = "body to pass with the api call"),
    }, rawResponse = true)
    public String callApi(String rawResponse) {

        DataObject dataObject = DataObject.fromJson(rawResponse).getObject("parameters");
        String url = dataObject.getString("api_url");
        String callType = dataObject.getString("call_type");
        String body = dataObject.getString("body", null);

        DataObject headersObject = dataObject.getObject("headers");
        HashMap<String, String> headers = new HashMap<>();
        for (String key : headersObject.keys()) {
            headers.put(key, headersObject.getString(key));
        }

        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url(url);

        headers.forEach(requestBuilder::addHeader);

        if (callType.equalsIgnoreCase("GET")) {
            requestBuilder.get().build();
        } else if (callType.equalsIgnoreCase("POST")) {

            try {
                requestBuilder.post(RequestBody.create(body.getBytes())).build();
            } catch (Exception e) {
                return "Error in body of the request \n\n" + rawResponse;
            }
        }

        try {
            Response response = client.newCall(requestBuilder.build()).execute();
            String string = response.body().string();

            if (isJSONValid(string)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement jsonElement = JsonParser.parseString(string);
                string = gson.toJson(jsonElement);
            }

            return string;
        } catch (Exception e) {
            return "Error in calling the api \n\n" + rawResponse;
        }
    }

    @Tool(name = "ai", description = "Simple AI to respond to messages", parameters = {
            @Parameter(name = "message", Type = "string", description = "Message to respond to"),
            @Parameter(name = "user_id", Type = "string", description = "User ID")
    })
    public String ai(String message, String userId) {
        return OllamaManager.getInstance().createAssistant(null, userId).chat(userId, message);
    }

    public static boolean isJSONValid(String jsonInString ) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
