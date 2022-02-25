package dev.sbs.simplifiedbot.command.group.developer;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;

@CommandInfo(
    id = "07cff179-8415-4612-a15e-4534de379051",
    name = "activity",
    parent = DevCommand.class
)
public class DevActivityCommand extends Command {

    protected DevActivityCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) throws DiscordException {

    }

}