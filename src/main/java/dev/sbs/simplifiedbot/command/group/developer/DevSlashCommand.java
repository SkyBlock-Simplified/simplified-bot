package dev.sbs.simplifiedbot.command.group.developer;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "e7d01ff9-13f5-4b42-97c0-8c04faadf8ba",
    name = "slash",
    parent = DevCommand.class
)
public class DevSlashCommand extends Command {

    protected DevSlashCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) throws DiscordException {
        return this.getDiscordBot().getCommandRegistrar().updateSlashCommands();
    }

}
