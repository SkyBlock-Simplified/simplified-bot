package dev.sbs.simplifiedbot;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.sbs.request.SkyBlockRequest;
import dev.sbs.api.client.sbs.response.SkyBlockEmojis;
import dev.sbs.api.data.model.discord.emojis.EmojiModel;
import dev.sbs.api.data.model.discord.emojis.EmojiSqlModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildSqlModel;
import dev.sbs.api.data.sql.SqlConfig;
import dev.sbs.api.reflection.Reflection;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentSet;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.reference.CommandReference;
import dev.sbs.simplifiedbot.util.ItemCache;
import dev.sbs.simplifiedbot.util.SimplifiedConfig;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.ShardInfo;
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
    private SkyBlockEmojis skyBlockEmojis;

    public static void main(final String[] args) {
        new SimplifiedBot();
    }

    private SimplifiedBot() {
        super(new SimplifiedConfig(new SqlConfig(), "simplified-discord"));
    }

    @Override
    protected @NotNull ConcurrentSet<Class<? extends CommandReference>> getCommands() {
        return Concurrent.newUnmodifiableSet(
            Reflection.getResources()
                .filterPackage("dev.sbs.simplifiedbot.command")
                .getSubtypesOf(CommandReference.class)
        );
    }

    @Override
    protected @NotNull AllowedMentions getDefaultAllowedMentions() {
        return AllowedMentions.suppressEveryone();
    }

    @Override
    public @NotNull IntentSet getDisabledIntents() {
        return IntentSet.of(Intent.GUILD_PRESENCES);
    }

    @Override
    protected @NotNull ClientPresence getInitialPresence(ShardInfo shardInfo) {
        return ClientPresence.online(ClientActivity.watching("debugging"));
    }

    @Override
    protected void onDatabaseConnected() {
        // Update Emojis
        log.info("Updating Emojis");
        ConcurrentList<EmojiModel> allEmojis = SimplifiedApi.getRepositoryOf(EmojiModel.class).findAll();
        SimplifiedApi.getRepositoryOf(GuildSqlModel.class)
            .matchAll(GuildModel::isEmojiServer)
            .stream()
            .flatMap(guildModel -> this.getGateway()
                .getGuildEmojis(Snowflake.of(guildModel.getGuildId()))
                .filter(guildEmoji -> allEmojis.stream().noneMatch(emojiModel -> guildEmoji.getId().asLong() == emojiModel.getEmojiId()))
                .map(guildEmoji -> Pair.of(guildModel, guildEmoji))
                .collect(Concurrent.toList())
                .blockOptional()
                .orElse(Concurrent.newList())
                .stream()
            )
            .forEach(guildEmoji -> {
                EmojiSqlModel emojiSqlModel = new EmojiSqlModel();
                emojiSqlModel.setGuild(guildEmoji.getKey());
                emojiSqlModel.setEmojiId(guildEmoji.getRight().getId().asLong());
                emojiSqlModel.setKey(guildEmoji.getRight().getName().replace(" ", "_").toUpperCase());
                emojiSqlModel.setName(StringUtil.capitalizeFully(guildEmoji.getRight().getName().replace("_", " ")));
                emojiSqlModel.setAnimated(guildEmoji.getRight().isAnimated());
                emojiSqlModel.save();
            });

        // Update Caches
        log.info("Building Caches");
        this.skyBlockEmojis = SimplifiedApi.getWebApi(SkyBlockRequest.class).getItemEmojis();
        this.itemCache = new ItemCache();
        this.getItemCache().getAuctionHouse().update();
        this.getItemCache().getBazaar().update();
        this.getItemCache().getEndedAuctions().update();

        // Schedule SkyBlock Emoji Cache Updates
        this.getScheduler().scheduleAsync(
            () -> this.skyBlockEmojis = SimplifiedApi.getWebApi(SkyBlockRequest.class).getItemEmojis(),
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

    @Override
    protected void onGatewayConnected(@NotNull GatewayDiscordClient gatewayDiscordClient) {
        log.info("Creating Database Session");
        SimplifiedApi.getSessionManager().connectSql((SqlConfig) this.getConfig().getDataConfig());
        log.info(
            "Database Connected. (Initialized in {}ms, Started in {}ms",
            SimplifiedApi.getSessionManager().getSession().getInitializationTime(),
            SimplifiedApi.getSessionManager().getSession().getStartupTime()
        );
        this.onDatabaseConnected();
    }

    @Override
    protected void onGatewayDisconnected() {
        SimplifiedApi.getSessionManager().disconnect();
    }

}
