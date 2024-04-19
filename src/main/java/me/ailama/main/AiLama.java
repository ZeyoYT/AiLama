package me.ailama.main;

import okhttp3.OkHttpClient;

import java.awt.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiLama
{
    private static AiLama INSTANCE;

    private final Color defaultColor = new Color(0, 186, 73);
    private final OkHttpClient okHttpClient;

    public AiLama() {
        this.okHttpClient = new OkHttpClient();
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public OkHttpClient getOkHttpClient() {
        return this.okHttpClient;
    }

    public String capitalizeFirstLetter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public boolean isNumber(final String number) {
        try {
            Integer.parseInt(number);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public Object ifNull(Object object, Object ifNull) {
        return object == null ? ifNull : object;
    }

    public int random(int max , int min)
    {
        return (int)(Math.random()*(max-min+1)+min);
    }

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

    public static AiLama getInstance() {
        if (AiLama.INSTANCE == null) {
            AiLama.INSTANCE = new AiLama();
        }
        return AiLama.INSTANCE;
    }

    // delay in milliseconds
    public void wait(long delay, boolean threadDelay, boolean logStartEnd) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime targetTime = currentTime.plusNanos(delay * 1000000L);
        if (logStartEnd) {
            System.out.println(currentTime + " : " + targetTime);
        }

        if (threadDelay) {
            try {
                Thread.sleep(delay);
                if (logStartEnd) {
                    System.out.println(targetTime);
                }
            } catch (InterruptedException var8) {
                var8.printStackTrace();
            }

        } else {
            while(!currentTime.isEqual(targetTime) && !currentTime.isAfter(targetTime)) {
                currentTime = LocalDateTime.now();
            }

            if (logStartEnd) {
                System.out.println(targetTime);
            }

        }
    }

    public List<Object> convertObjectToList(Object o) {
        if(o instanceof List) {
            return new ArrayList<>((List<?>) o);
        }

        return null;
    }

    public List<String> getParts(final String string, final int partitionSize) {
        final List<String> parts = new ArrayList<>();
        for (int len = string.length(), i = 0; i < len; i += partitionSize) {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }

    public boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String fixUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    public boolean randomChoice() {
        return Math.random() < 0.5;
    }
}
