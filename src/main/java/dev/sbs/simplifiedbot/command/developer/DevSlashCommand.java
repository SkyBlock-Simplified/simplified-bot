package dev.sbs.simplifiedbot.command.developer;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandId;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("e7d01ff9-13f5-4b42-97c0-8c04faadf8ba")
public class DevSlashCommand extends Command {

    protected DevSlashCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {
        return this.getDiscordBot().getCommandRegistrar().updateSlashCommands();
    }

}
