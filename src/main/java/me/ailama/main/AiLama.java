package me.ailama.main;

import me.ailama.handler.annotations.Tool;
import me.ailama.handler.annotations.Parameter;
import org.joda.time.DateTime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AiLama
{
    private static AiLama INSTANCE;

    private DateTime starterTime;

    public AiLama() {

    }

    @Tool(name = "fixUrl", description = "Fix a URL if it doesn't start with 'http://' or 'https://'", parameters = {
            @Parameter(name = "url", Type = "string")
    })
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

    // delay in milliseconds
    // threadDelay: true if you want to use Thread.sleep() instead of a while loop
    // logStartEnd: true if you want to log the start and end time
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

    public void startTimer() {
        this.starterTime = DateTime.now();
    }

    public long getElapsedTime() {
        return DateTime.now().getMillis() - this.starterTime.getMillis();
    }

    public static AiLama getInstance() {
        if (AiLama.INSTANCE == null) {
            AiLama.INSTANCE = new AiLama();
        }
        return AiLama.INSTANCE;
    }

}
