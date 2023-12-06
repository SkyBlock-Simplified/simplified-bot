package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.command_data.commands.CommandModel;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.impl.SlashCommand;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
public abstract class SqlSlashCommand extends SlashCommand {

    private final @NotNull CommandModel config;
    private final @NotNull Optional<Parent> parent;
    private final @NotNull Optional<Group> group;

    public SqlSlashCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
        this.config = SimplifiedApi.getRepositoryOf(CommandModel.class).findFirstOrNull(CommandModel::getUniqueId, this.getUniqueId());
        this.parent = Optional.ofNullable(this.config.getParent()).map(parentModel -> Parent.of(parentModel.getKey(), parentModel.getDescription()));
        this.group = Optional.ofNullable(this.config.getGroup()).map(parentModel -> Group.of(parentModel.getKey(), parentModel.getDescription()));
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
    public final @NotNull String getName() {
        return this.getConfig().getName();
    }

}
