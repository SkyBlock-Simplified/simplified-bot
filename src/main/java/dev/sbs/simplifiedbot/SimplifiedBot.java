package dev.sbs.simplifiedbot;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.sbs.request.SbsRequest;
import dev.sbs.api.client.sbs.response.SkyBlockEmojis;
import dev.sbs.api.data.model.discord.emojis.EmojiModel;
import dev.sbs.api.data.model.discord.emojis.EmojiSqlModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildSqlModel;
import dev.sbs.api.data.sql.SqlConfig;
import dev.sbs.api.reflection.Reflection;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.data.tuple.pair.Pair;
import dev.sbs.api.util.helper.NumberUtil;
import dev.sbs.api.util.helper.ResourceUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.reference.CommandReference;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.util.DiscordConfig;
import dev.sbs.discordapi.util.DiscordEnvironment;
import dev.sbs.simplifiedbot.util.ItemCache;
import discord4j.common.util.Snowflake;
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
    private SkyBlockEmojis skyBlockEmojis;

    private SimplifiedBot(@NotNull DiscordConfig discordConfig) {
        super(discordConfig);
    }

    public static void main(final String[] args) {
        DiscordConfig.builder()
            .withFileName("config")
            .withEnvironment(DiscordEnvironment.PRODUCTION)
            .withToken(ResourceUtil.getEnv("DISCORD_TOKEN"))
            .withMainGuildId(652148034448261150L)
            .withDebugChannelId(ResourceUtil.getEnv("DEVELOPER_ERROR_LOG_CHANNEL_ID").map(NumberUtil::tryParseLong))
            .withDataConfig(SqlConfig.defaultSql())
            .withEmojiLocator(key -> SimplifiedApi.getRepositoryOf(EmojiModel.class)
                .findFirst(EmojiModel::getKey, key)
                .flatMap(Emoji::of)
            )
            .withCommands(
                Reflection.getResources()
                    .filterPackage("dev.sbs.simplifiedbot.command")
                    .getSubtypesOf(CommandReference.class)
            )
            .withAllowedMentions(AllowedMentions.suppressEveryone())
            .withDisabledIntents(IntentSet.of(Intent.GUILD_PRESENCES))
            .withClientPresence(ClientPresence.online(ClientActivity.watching("commands")))
            .withMemberRequestFilter(MemberRequestFilter.withLargeGuilds())
            .onGatewayConnected(gatewayDiscordClient -> ResourceUtil.getEnv("HYPIXEL_API_KEY")
                .map(StringUtil::toUUID)
                .ifPresent(value -> SimplifiedApi.getKeyManager().add("HYPIXEL_API_KEY", value))
            )
            //.onDatabaseConnected(SimplifiedBot::onDatabaseConnected)
            .build()
            .createBot(SimplifiedBot::new);
    }

    private void onDatabaseConnected() {
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
        this.skyBlockEmojis = SimplifiedApi.getApiRequest(SbsRequest.class).getItemEmojis();
        this.itemCache = new ItemCache();
        this.getItemCache().getAuctionHouse().update();
        this.getItemCache().getBazaar().update();
        this.getItemCache().getEndedAuctions().update();

        // Schedule SkyBlock Emoji Cache Updates
        this.getScheduler().scheduleAsync(
            () -> this.skyBlockEmojis = SimplifiedApi.getApiRequest(SbsRequest.class).getItemEmojis(),
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
