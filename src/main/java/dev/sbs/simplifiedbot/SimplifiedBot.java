package dev.sbs.simplifiedbot;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.persistence.JpaConfig;
import dev.sbs.api.reflection.Reflection;
import dev.sbs.api.util.NumberUtil;
import dev.sbs.api.util.SystemUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.DiscordCommand;
import dev.sbs.discordapi.handler.DiscordConfig;
import dev.sbs.minecraftapi.client.sbs.SbsClient;
import dev.sbs.minecraftapi.client.sbs.response.SkyBlockEmojiData;
import dev.sbs.simplifiedbot.util.ItemCache;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.shard.MemberRequestFilter;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.AllowedMentions;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@Getter
@Log4j2
public final class SimplifiedBot extends DiscordBot {

    private ItemCache itemCache;
    private SkyBlockEmojiData skyBlockEmojis;

    private SimplifiedBot(@NotNull DiscordConfig discordConfig) {
        super(discordConfig);
    }

    private static SimplifiedBot create(@NotNull DiscordConfig discordConfig) {
        SimplifiedBot simplifiedBot = new SimplifiedBot(discordConfig);
        simplifiedBot.start();
        return simplifiedBot;
    }

    public static void main(final String[] args) {
        DiscordConfig discordConfig = DiscordConfig.builder()
            .withToken(SystemUtil.getEnv("DISCORD_TOKEN"))
            .withMainGuildId(652148034448261150L)
            .withLogChannelId(SystemUtil.getEnv("DEVELOPER_ERROR_LOG_CHANNEL_ID").map(NumberUtil::tryParseLong))
            .withJpaConfig(JpaConfig.commonSql())
            .withCommands(
                Reflection.getResources()
                    .filterPackage("dev.sbs.simplifiedbot.command")
                    .getTypesOf(DiscordCommand.class)
            )
            .withEmojis(Reflection.getResources(SimplifiedBot.class.getClassLoader()).getResources("emojis/"))
            .withAllowedMentions(AllowedMentions.suppressEveryone())
            .withDisabledIntents(IntentSet.of(Intent.GUILD_PRESENCES))
            .withClientPresence(ClientPresence.online(ClientActivity.watching("commands")))
            .withMemberRequestFilter(MemberRequestFilter.withLargeGuilds())
            .build();

        SimplifiedBot.create(discordConfig);
    }

    @Override
    protected void onGatewayConnected(@NotNull GatewayDiscordClient gatewayDiscordClient) {
        SimplifiedApi.getKeyManager().add(SystemUtil.getEnvPair("HYPIXEL_API_KEY"));
    }

    @Override
    protected void onDatabaseConnected() {
        // Update Caches
        log.info("Building Caches");
        this.skyBlockEmojis = SimplifiedApi.getClient(SbsClient.class).getEndpoint().getItemEmojis();
        this.itemCache = new ItemCache();
        this.getItemCache().getAuctionHouse().update();
        this.getItemCache().getBazaar().update();
        this.getItemCache().getEndedAuctions().update();

        // Schedule SkyBlock Emoji Cache Updates
        this.getScheduler().scheduleAsync(
            () -> this.skyBlockEmojis = SimplifiedApi.getClient(SbsClient.class).getEndpoint().getItemEmojis(),
            10,
            10,
            TimeUnit.MINUTES
        );

        // Schedule Item Cache Updates
        this.getScheduler().scheduleAsync(() -> {
            this.getItemCache().getAuctionHouse().update();
            this.getItemCache().getBazaar().update();
            this.getItemCache().getEndedAuctions().update();
        }, 1, 1, TimeUnit.SECONDS);
    }

}
