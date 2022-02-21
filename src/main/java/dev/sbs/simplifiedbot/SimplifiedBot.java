package dev.sbs.simplifiedbot;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.emojis.EmojiModel;
import dev.sbs.api.data.model.discord.emojis.EmojiSqlModel;
import dev.sbs.api.data.model.discord.guilds.GuildModel;
import dev.sbs.api.data.model.discord.guilds.GuildSqlModel;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentSet;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.PrefixCommand;
import dev.sbs.discordapi.util.DiscordConfig;
import dev.sbs.simplifiedbot.command.AboutCommand;
import dev.sbs.simplifiedbot.command.DevCommand;
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
import dev.sbs.simplifiedbot.command.group.player.PlayerDungeonClassesCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerDungeonsCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerNetworthCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerPetsCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerSkillsCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerSlayersCommand;
import dev.sbs.simplifiedbot.command.group.player.PlayerWeightCommand;
import dev.sbs.simplifiedbot.command.prefix.SbsCommand;
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
            PlayerDungeonClassesCommand.class,
            PlayerDungeonsCommand.class,
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
        SimplifiedApi.getRepositoryOf(GuildModel.class)
            .matchAll(GuildModel::isEmojiServer)
            .sort(GuildModel::getId)
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
                        } else {
                            if (existingEmojiModel.getEmojiId() != guildEmoji.getId().asLong()) {
                                EmojiSqlModel emojiSqlModel = (EmojiSqlModel) existingEmojiModel;
                                emojiSqlModel.setEmojiId(guildEmoji.getId().asLong());
                                emojiSqlModel.update();
                                this.getLog().info("Updating emoji: {0} :: {1,number,#} :: {2}", guild.getName(), guildEmoji.getId().asLong(), name);
                            }
                        }
                    })
                )
            );
    }

}
