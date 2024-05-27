package me.ailama.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.ailama.handler.annotations.Args;
import me.ailama.handler.annotations.Tool;
import me.ailama.main.AiLama;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;

public class ApiTools {
    @Tool(name = "currencyRate", description = "Converts a currency rate to another", arguments = {
            @Args(name = "amount", Type = "double", description = "Amount to convert", noNull = true),
            @Args(name = "currency1", Type = "string", description = "Currency to convert from, Like INR"),
            @Args(name = "currency2", Type = "string", description = "Currency to convert to, Like USD")
    })
    public String currencyRate(Double amount, String currency1, String currency2) {
        final double finalRate = Double.parseDouble(AiLama.getInstance().getRates(currency1.toLowerCase(),currency2.toLowerCase()));
        final double conv = finalRate * amount;
        return String.format("%.4f", conv);
    }

    @Tool(name = "getApi", description = "used for calling api", arguments = {
            @Args(name = "api_url", Type = "string", description = "full api url, like https://api.example.com"),
            @Args(name = "headers", Type = "json_object", description = "headers to pass for the api call"),
            @Args(name = "call_type", Type = "STRING{GET,POST}", description = "define the api call type"),
            @Args(name = "body", Type = "string", description = "body to pass with the api call"),
    }, rawResponse = true)
    public String callGetApi(String rawResponse) {

        DataObject dataObject = DataObject.fromJson(rawResponse).getObject("arguments");
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
