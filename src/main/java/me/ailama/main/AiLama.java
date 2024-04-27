package me.ailama.main;

import me.ailama.handler.annotations.Tool;
import me.ailama.handler.annotations.Args;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AiLama
{
    private static AiLama INSTANCE;

    public AiLama() {

    }

    public String capitalizeFirstLetter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    @Tool(name = "add", description = "Addition ('+') of two numbers like N1+N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String add(Number a, Number b) {
        return String.valueOf(a.doubleValue() + b.doubleValue());
    }

    @Tool(name = "subtract", description = "Subtraction ('-') of two numbers like N1-N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String subtract(Number a, Number b) {
        return String.valueOf(a.doubleValue() - b.doubleValue());
    }

    @Tool(name = "multiply", description = "Multiplication ('*') of two numbers like N1*N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String multiply(Number a, Number b) {
        return String.valueOf(a.doubleValue() * b.doubleValue());
    }

    @Tool(name = "divide", description = "Division ('/') of two numbers like N1/N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String divide(Number a, Number b) {
        return String.valueOf(a.doubleValue() / b.doubleValue());
    }

    @Tool(name = "modulus", description = "Modulus ('%') of two numbers like N1%N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String modulus(Number a, Number b) {
        return String.valueOf(a.doubleValue() % b.doubleValue());
    }

    @Tool(name = "power", description = "Power ('^') of two numbers like N1^N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String power(Number a, Number b) {
        return String.valueOf(Math.pow(a.doubleValue(), b.doubleValue()));
    }

    @Tool(name = "sqrt", description = "Square root of a number like sqrt(N1)", arguments = {
            @Args(name = "a", Type = "number")
    })
    public String sqrt(Number a) {
        return String.valueOf(Math.sqrt(a.doubleValue()));
    }

    @Tool(name = "cbrt", description = "Cube root of a number like cbrt(N1)", arguments = {
            @Args(name = "a", Type = "number")
    })
    public String cbrt(Number a) {
        return String.valueOf(Math.cbrt(a.doubleValue()));
    }

    @Tool(name = "formatTime", description = "Format time from milliseconds to a readable format like formatTime(N1)", arguments = {
            @Args(name = "timeInMillis", Type = "number")
    })
    public String formatTime(final long timeInMillis) {

        int seconds = (int) (timeInMillis / 1000) % 60;
        int minutes = (int) ((timeInMillis / (1000*60)) % 60);
        int hours   = (int) ((timeInMillis / (1000*60*60)) % 24);
        int days = (int) (timeInMillis / (1000*60*60*24));

        if (days > 0) {
            return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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

    @Tool(name = "time", description = "Get the current time in a specific timezone like time(is24Hour, timeZone)", arguments = {
            @Args(name = "is24Hour", Type = "boolean", description = "true for 24-hour format, false for 12-hour format"),
            @Args(name = "timeZone", Type = "string", description = "Timezone in which you want to get the time like 'Asia/Kolkata'")
    })
    public String time(boolean is24Hour, String timeZone) {
        DateTime dateTime = new DateTime();
        if (timeZone != null) {
            dateTime = dateTime.withZone(DateTimeZone.forID(timeZone));
        }
        return dateTime.toString(is24Hour ? "HH:mm:ss" : "hh:mm:ss a");
    }

    @Tool(name = "currencyRate", description = "Converts a currency rate to another like currencyRate(amount, currency1, currency2)", arguments = {
            @Args(name = "amount", Type = "double", description = "Amount to convert"),
            @Args(name = "currency1", Type = "string", description = "Currency to convert from, Like INR"),
            @Args(name = "currency2", Type = "string", description = "Currency to convert to, Like USD")
    })
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
            final ResponseBody responseBody = okHttpClient.newCall(request).execute().body();
            if (responseBody != null) {
                final DataObject dataObject = DataObject.fromJson(responseBody.string());
                rate = dataObject.getObject(currency).getString(currency2);
            }
            return rate;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
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

    @Tool(name = "isValidURL", description = "Check if a URL is valid like isValidURL(url)", arguments = {
            @Args(name = "url", Type = "string")
    })
    public boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static AiLama getInstance() {
        if (AiLama.INSTANCE == null) {
            AiLama.INSTANCE = new AiLama();
        }
        return AiLama.INSTANCE;
    }

}
