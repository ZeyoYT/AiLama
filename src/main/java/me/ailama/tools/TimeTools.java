package me.ailama.tools;

import me.ailama.handler.annotations.Parameter;
import me.ailama.handler.annotations.Tool;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class TimeTools {

        @Tool(name = "formatTime", description = "Format time from milliseconds to a readable format like formatTime(N1)", parameters = {
                @Parameter(name = "timeInMillis", Type = "number")
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

        @Tool(name = "time", description = "Get the current time", parameters = {
                @Parameter(name = "is24Hour", Type = "boolean", description = "true for 24-hour format, false for 12-hour format"),
                @Parameter(name = "timeZone", Type = "string", description = "Timezone in which you want to get the time like 'Asia/Kolkata'")
        })
        public String time(boolean is24Hour, String timeZone) {
            DateTime dateTime = new DateTime();
            if (timeZone != null) {
                dateTime = dateTime.withZone(DateTimeZone.forID(timeZone));
            }

            return dateTime.toString(is24Hour ? "HH:mm:ss" : "hh:mm:ss a");
        }

}
