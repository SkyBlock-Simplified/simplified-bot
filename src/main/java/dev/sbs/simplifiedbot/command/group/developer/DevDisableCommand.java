package dev.sbs.simplifiedbot.command.group.developer;

import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;
import org.jetbrains.annotations.NotNull;

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
    protected void process(CommandContext<?> commandContext) throws DiscordException {

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
            )
        );
    }

}
