package dev.sbs.simplifiedbot.command.group.developer;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;

@CommandInfo(
    id = "4be179c5-c163-47e9-a682-7c6f237d9437",
    name = "shards",
    parent = DevCommand.class
)
public class DevShardCommand extends Command {

    protected DevShardCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) throws DiscordException {

    }

}