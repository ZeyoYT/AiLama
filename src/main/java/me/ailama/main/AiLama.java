package me.ailama.main;

import me.ailama.handler.annotations.Tool;
import me.ailama.handler.annotations.Args;

import java.lang.reflect.Method;
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

    @Tool(name = "abs", description = "Absolute value of a number like abs(N1)", arguments = {
            @Args(name = "a", Type = "number")
    })
    public String abs(Number a) {
        return String.valueOf(Math.abs(a.doubleValue()));
    }

    @Tool(name = "round", description = "Round a number like round(N1)", arguments = {
            @Args(name = "a", Type = "number")
    })
    public String round(Number a) {
        return String.valueOf(Math.round(a.doubleValue()));
    }

    @Tool(name = "ceil", description = "Ceil a number like ceil(N1)", arguments = {
            @Args(name = "a", Type = "number")
    })
    public String ceil(Number a) {
        return String.valueOf(Math.ceil(a.doubleValue()));
    }

    @Tool(name = "floor", description = "Floor a number like floor(N1)", arguments = {
            @Args(name = "a", Type = "number")
    })
    public String floor(Number a) {
        return String.valueOf(Math.floor(a.doubleValue()));
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


    public String fixUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
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
