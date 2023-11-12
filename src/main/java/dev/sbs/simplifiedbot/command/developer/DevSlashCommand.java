package dev.sbs.simplifiedbot.command.developer;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.context.interaction.deferrable.application.slash.SlashCommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.util.SqlSlashCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("e7d01ff9-13f5-4b42-97c0-8c04faadf8ba")
public class DevSlashCommand extends SqlSlashCommand {

    protected DevSlashCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return this.getDiscordBot().getCommandRegistrar().updateApplicationCommands();
    }

}
