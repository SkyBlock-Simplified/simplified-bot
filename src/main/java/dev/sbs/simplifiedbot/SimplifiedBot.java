package dev.sbs.simplifiedbot;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.sbs.implementation.SkyBlockData;
import dev.sbs.api.client.sbs.response.SkyBlockEmojisResponse;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentSet;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.PrefixCommand;
import dev.sbs.discordapi.util.DiscordConfig;
import dev.sbs.simplifiedbot.command.AboutCommand;
import dev.sbs.simplifiedbot.command.DevCommand;
import dev.sbs.simplifiedbot.command.DungeonsCommand;
import dev.sbs.simplifiedbot.command.GuildCommand;
import dev.sbs.simplifiedbot.command.HelpCommand;
import dev.sbs.simplifiedbot.command.MissingCommand;
import dev.sbs.simplifiedbot.command.PlayerCommand;
import dev.sbs.simplifiedbot.command.VerifyCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevActivityCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevLatencyCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevShardCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevStatsCommand;
import dev.sbs.simplifiedbot.command.group.developer.DevTestCommand;
import dev.sbs.simplifiedbot.command.group.developer.command.DevDisableCommand;
import dev.sbs.simplifiedbot.command.group.developer.command.DevEnableCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerNetworthCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerPetsCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerSkillsCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerSlayersCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerWeightCommand;
import dev.sbs.simplifiedbot.command.prefix.SbsCommand;
import dev.sbs.simplifiedbot.util.ItemCache;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.AllowedMentions;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class SimplifiedBot extends DiscordBot {

    @Getter private DiscordConfig config;
    @Getter private ItemCache itemCache;
    @Getter private SkyBlockEmojisResponse skyBlockEmojis;

    public static void main(final String[] args) {
        new SimplifiedBot();
    }

    @Override
    protected @NotNull ConcurrentSet<Class<? extends Command>> getCommands() {
        return Concurrent.newUnmodifiableSet(
            // Top-Level/Slash Commands
            AboutCommand.class,
            DevCommand.class,
            GuildCommand.class,
            HelpCommand.class,
            MissingCommand.class,
            PlayerCommand.class,
            VerifyCommand.class,

            // Developer Commands
            DevDisableCommand.class,
            DevEnableCommand.class,
            DevActivityCommand.class,
            DevLatencyCommand.class,
            DevShardCommand.class,
            DevStatsCommand.class,
            DevTestCommand.class,

            // Player Commands
            DungeonsCommand.class,
            PlayerNetworthCommand.class,
            PlayerPetsCommand.class,
            PlayerSkillsCommand.class,
            PlayerSlayersCommand.class,
            PlayerWeightCommand.class
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
    protected @NotNull Class<? extends PrefixCommand> getPrefixCommand() {
        return SbsCommand.class;
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
        // Update Caches
        this.skyBlockEmojis = SimplifiedApi.getWebApi(SkyBlockData.class).getEmojis();
        this.itemCache = new ItemCache();
        this.getItemCache().getAuctionHouse().update();
        this.getItemCache().getBazaar().update();
        this.getItemCache().getEndedAuctions().update();

        // Schedule SkyBlock Emoji Cache Updates
        this.getScheduler().scheduleAsync(
            () -> this.skyBlockEmojis = SimplifiedApi.getWebApi(SkyBlockData.class).getEmojis(),
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
