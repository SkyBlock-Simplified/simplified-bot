package dev.sbs.simplifiedbot.command.group.developer.command;

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
    id = "5f279de3-6cf2-468e-9ec1-b208637efc8c",
    name = "enable",
    parent = DevCommand.class
)
public class DevEnableCommand extends Command {

    protected DevEnableCommand(DiscordBot discordBot) {
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
                "The command to enable.",
                Parameter.Type.WORD,
                true
            )
        );
    }

}
