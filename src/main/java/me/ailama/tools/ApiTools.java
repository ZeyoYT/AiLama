package me.ailama.tools;

import me.ailama.handler.annotations.Args;
import me.ailama.handler.annotations.Tool;
import me.ailama.main.AiLama;

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
}
