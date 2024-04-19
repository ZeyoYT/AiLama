package me.ailama.main;

import me.ailama.autoevents.commandhandler.HandleCommand;
import me.ailama.autoevents.jdaevents.ButtonEvent;
import me.ailama.autoevents.jdaevents.JDAReady;
import me.ailama.autoevents.jdaevents.MenuEvent;
import me.ailama.autoevents.jdaevents.ModalEvent;
import me.ailama.config.Config;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
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
                .setMemberCachePolicy(MemberCachePolicy.ALL)

                .addEventListeners(
                        new JDAReady(),

                        new HandleCommand(),

                        new ModalEvent(),
                        new MenuEvent(),
                        new ButtonEvent()
                )

                .setEnabledIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.SCHEDULED_EVENTS,
                        GatewayIntent.GUILD_MODERATION,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.DIRECT_MESSAGES
                )
                .enableCache(
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.VOICE_STATE
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

    public static void main(String[] args) {
        getInstance();
    }

    public static Main getInstance() {

        if(instance == null) {
            instance = new Main();
        }

        return instance;
    }
}