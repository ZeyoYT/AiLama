package me.ailama.config;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    public static final Dotenv dotenv = Dotenv.load();

    public static String token;

    public static String get(String key)
    {
        return dotenv.get(key.toUpperCase());
    }

    public static String getToken(String key)
    {
        token = dotenv.get(key.toUpperCase());
        return token;
    }
}
