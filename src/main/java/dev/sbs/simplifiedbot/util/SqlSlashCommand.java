package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.command_data.commands.CommandModel;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.impl.SlashCommand;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public abstract class SqlSlashCommand extends SlashCommand {

    @Getter private final @NotNull CommandModel config;

    public SqlSlashCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
        this.config = SimplifiedApi.getRepositoryOf(CommandModel.class).findFirstOrNull(CommandModel::getUniqueId, this.getUniqueId());
    }

    @Override
    public final boolean isEnabled() {
        return this.getConfig().isEnabled();
    }

    @Override
    public final @NotNull String getDescription() {
        return this.getConfig().getDescription();
    }

    @Override
    public final long getGuildId() {
        return this.getConfig().getGuild() != null ? this.getConfig().getGuild().getGuildId() : 0L;
    }

    @Override
    public final long getId() {
        return 0; // TODO: Get command id
    }

    @Override
    public final @NotNull String getName() {
        return this.getConfig().getName();
    }

}
