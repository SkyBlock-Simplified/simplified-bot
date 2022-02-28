package dev.sbs.simplifiedbot.command.group.developer.command;

import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "79dbe55c-22a5-41dc-9bb0-fc956e2390b3",
    name = "disable",
    parent = DevCommand.class
)
public class DevDisableCommand extends Command {

    protected DevDisableCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) throws DiscordException {
        return Mono.empty();
    }

    @NotNull
    @Override
    public ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            new Parameter(
                "command",
                "The command to disable.",
                Parameter.Type.WORD,
                true
            ),
            new Parameter(
                "reason",
                "The reason the command is being disabled.",
                Parameter.Type.TEXT
            )
        );
    }

}
