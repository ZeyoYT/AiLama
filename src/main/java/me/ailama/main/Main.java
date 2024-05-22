package me.ailama.main;

import me.ailama.autoevents.commandhandler.HandleCommand;
import me.ailama.autoevents.jdaevents.JDAReady;
import me.ailama.config.Config;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private final ShardManager shardManager;
    public static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static Main instance;

    public Main() {
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(Config.getToken("TOKEN"))
                .setActivity(Activity.watching("User"))
                .setAutoReconnect(true)
                .setMemberCachePolicy(MemberCachePolicy.DEFAULT)

                .addEventListeners(
                        new JDAReady(),
                        new HandleCommand()
                )

                .setEnabledIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.SCHEDULED_EVENTS,
                        GatewayIntent.MESSAGE_CONTENT
                )

                .enableCache(
                        CacheFlag.VOICE_STATE,
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.SCHEDULED_EVENTS,
                        CacheFlag.EMOJI
                );

        shardManager = builder.build();

        if(shardManager.retrieveUserById(Config.get("DEV_ID")).complete() != null) {
            LOGGER.info("Bot is ready to use");
        }
        else {
            LOGGER.error("Bot is not ready to use because the developer id is not valid. Please check the developer id in the config file.");
            System.exit(0);
        }
    }

    public boolean checkRequiredEnvironmentArgs() {

        boolean flag = true;

        if(Config.get("TOKEN") == null) {
            LOGGER.error("TOKEN environment variable is missing");
            flag = false;
        }

        if(Config.get("OLLAMA_URL") == null) {
            LOGGER.error("OLLAMA_URL environment variable is missing");
            flag = false;
        }

        if(Config.get("OLLAMA_MODEL") == null) {
            LOGGER.error("OLLAMA_MODEL environment variable is missing");
            flag = false;
        }

        if(Config.get("OLLAMA_EMBEDDING_MODEL") == null) {
            LOGGER.error("OLLAMA_EMBEDDING_MODEL environment variable is missing");
            flag = false;
        }

        if(Config.get("DEV_ID") == null) {
            LOGGER.error("DEV_ID environment variable is missing");
            flag = false;
        }

        return flag;
    }

    public static void main(String[] args) {

        if(!getInstance().checkRequiredEnvironmentArgs()) {
            System.exit(0);
        }

        getInstance();
    }

    public static Main getInstance() {

        if(instance == null) {
            instance = new Main();
        }

        return instance;
    }
}