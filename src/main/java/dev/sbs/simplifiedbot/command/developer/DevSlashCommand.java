package dev.sbs.simplifiedbot.command.developer;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandStructure;
import dev.sbs.discordapi.command.SlashCommand;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.exception.DiscordException;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandStructure(
    parent = "dev",
    group = "command",
    name = "update"
)
public class DevSlashCommand extends SlashCommand {

    protected DevSlashCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return this.getDiscordBot().getCommandHandler().updateApplicationCommands();
    }

}
