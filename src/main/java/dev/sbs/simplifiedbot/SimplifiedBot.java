package dev.sbs.simplifiedbot;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.sbs.request.SkyBlockRequest;
import dev.sbs.api.client.sbs.response.SkyBlockEmojis;
import dev.sbs.api.data.model.discord.command_data.command_parents.CommandParentModel;
import dev.sbs.api.data.model.discord.emojis.EmojiModel;
import dev.sbs.api.data.model.discord.emojis.EmojiSqlModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildSqlModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentSet;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.util.DiscordConfig;
import dev.sbs.simplifiedbot.command.AboutCommand;
import dev.sbs.simplifiedbot.command.DungeonsCommand;
import dev.sbs.simplifiedbot.command.GuildCommand;
import dev.sbs.simplifiedbot.command.HelpCommand;
import dev.sbs.simplifiedbot.command.LinkCommand;
import dev.sbs.simplifiedbot.command.MissingCommand;
import dev.sbs.simplifiedbot.command.PlayerCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevActivityCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevLatencyCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevShardCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevStatsCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevTestCommand;
import dev.sbs.simplifiedbot.command.group.developer.command.DevDisableCommand;
import dev.sbs.simplifiedbot.command.group.developer.command.DevEnableCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerAccessoriesCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerAuctionsCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerJacobsCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerNetworthCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerPetsCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerSkillsCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerSlayersCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerWeightCommand;
import dev.sbs.simplifiedbot.command.group.reputation.RepCheckCommand;
import dev.sbs.simplifiedbot.command.group.reputation.RepGiveCommand;
import dev.sbs.simplifiedbot.util.ItemCache;
import discord4j.common.util.Snowflake;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.AllowedMentions;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class SimplifiedBot extends DiscordBot {

    @Getter private DiscordConfig config;
    @Getter private ItemCache itemCache;
    @Getter private SkyBlockEmojis skyBlockEmojis;

    public static void main(final String[] args) {
        new SimplifiedBot();
    }

    @Override
    protected @NotNull ConcurrentSet<Class<? extends Command>> getCommands() {
        return Concurrent.newUnmodifiableSet(
            // Top-Level/Slash Commands
            AboutCommand.class,
            GuildCommand.class,
            HelpCommand.class,
            MissingCommand.class,
            PlayerCommand.class,
            DungeonsCommand.class,
            LinkCommand.class,

            // Developer Commands
            DevDisableCommand.class,
            DevEnableCommand.class,
            DevActivityCommand.class,
            DevLatencyCommand.class,
            DevShardCommand.class,
            DevStatsCommand.class,
            DevTestCommand.class,

            // Player Commands
            PlayerAccessoriesCommand.class,
            PlayerAuctionsCommand.class,
            PlayerJacobsCommand.class,
            PlayerNetworthCommand.class,
            PlayerPetsCommand.class,
            PlayerSkillsCommand.class,
            PlayerSlayersCommand.class,
            PlayerWeightCommand.class,

            // Reputation Commands
            RepGiveCommand.class,
            RepCheckCommand.class
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
        return ClientPresence.online(ClientActivity.listening("beta"));
    }

    @Override
    protected @NotNull Optional<CommandParentModel> getPrefix() {
        return SimplifiedApi.getRepositoryOf(CommandParentModel.class).findFirst(CommandParentModel::getKey, "sbs");
    }

    @Override
    protected void loadConfig() {
        try {
            File currentDir = new File(SimplifiedApi.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            this.config = new DiscordConfig(currentDir.getParentFile(), "simplified-discord");
        } catch (Exception exception) {
            throw new IllegalArgumentException("Unable to retrieve current directory!", exception); // Should never get here
        }
    }

    @Override
    protected void onDatabaseConnected() {
        // Update Emojis
        this.getLog().info("Updating Emojis");
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
                emojiSqlModel.setName(WordUtil.capitalizeFully(guildEmoji.getRight().getName().replace("_", " ")));
                emojiSqlModel.setAnimated(guildEmoji.getRight().isAnimated());
                emojiSqlModel.save();
            });

        // Update Caches
        this.getLog().info("Building Caches");
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
    protected void onGatewayDisconnected() {
        SimplifiedApi.disconnectDatabase();
    }

}
