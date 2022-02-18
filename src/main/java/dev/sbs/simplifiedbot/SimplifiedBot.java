package dev.sbs.simplifiedbot;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentSet;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.PrefixCommand;
import dev.sbs.discordapi.util.DiscordConfig;
import dev.sbs.simplifiedbot.command.AboutCommand;
import dev.sbs.simplifiedbot.command.DeveloperCommand;
import dev.sbs.simplifiedbot.command.GuildCommand;
import dev.sbs.simplifiedbot.command.HelpCommand;
import dev.sbs.simplifiedbot.command.MissingCommand;
import dev.sbs.simplifiedbot.command.PlayerCommand;
import dev.sbs.simplifiedbot.command.VerifyCommand;
import dev.sbs.simplifiedbot.command.group.developer.DeveloperStatsCommand;
import dev.sbs.simplifiedbot.command.group.developer.DeveloperSubCommand;
import dev.sbs.simplifiedbot.command.prefix.SbsCommand;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.AllowedMentions;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class SimplifiedBot extends DiscordBot {

    @Getter private DiscordConfig config;

    public static void main(final String[] args) {
        new SimplifiedBot();
    }

    @Override
    protected @NotNull ConcurrentSet<Class<? extends Command>> getCommands() {
        return Concurrent.newUnmodifiableSet(
            // Top-Level/Slash Commands
            AboutCommand.class,
            DeveloperCommand.class,
            HelpCommand.class,
            PlayerCommand.class,
            GuildCommand.class,
            MissingCommand.class,
            VerifyCommand.class,

            // Sub Commands
            DeveloperSubCommand.class,
            DeveloperStatsCommand.class
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
        /*this.getLog().info("Adding emojis");
        SimplifiedApi.getRepositoryOf(GuildModel.class)
            .matchAll(GuildModel::getId, GuildModel::isEmojiServer)
            .forEach(guildModel -> this.getGateway()
                .getGuildById(Snowflake.of(guildModel.getGuildId()))
                .blockOptional()
                .ifPresent(guild -> guild.getEmojis()
                    .toStream()
                    .forEach(guildEmoji -> {
                        String key = guildEmoji.getName().toUpperCase();
                        String name = WordUtil.capitalizeFully(guildEmoji.getName().replace("_", " "));
                        EmojiModel existingEmojiModel = SimplifiedApi.getRepositoryOf(EmojiModel.class).findFirstOrNull(EmojiModel::getKey, key);

                        if (existingEmojiModel == null) {
                            EmojiSqlModel newEmojiModel = new EmojiSqlModel();
                            newEmojiModel.setEmojiId(guildEmoji.getId().asLong());
                            newEmojiModel.setGuild((GuildSqlModel) guildModel);
                            newEmojiModel.setKey(key);
                            newEmojiModel.setName(name);
                            newEmojiModel.save();
                            this.getLog().info("Saving new emoji: {0} :: {1,number,#} :: {2}", guild.getName(), guildEmoji.getId().asLong(), name);
                        }
                    })
                )
            );*/
    }

}
