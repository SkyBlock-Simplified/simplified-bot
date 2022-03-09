package dev.sbs.simplifiedbot.command.group.developer;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "67702b52-3f90-43d5-8c66-ac52613dc7aa",
    name = "latency",
    parent = DevCommand.class
)
public class DevLatencyCommand extends Command {

    protected DevLatencyCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {
        return Mono.empty();
    }

}
