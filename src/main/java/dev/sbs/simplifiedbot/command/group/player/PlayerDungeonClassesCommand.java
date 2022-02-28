package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "45378587-204e-423f-8dd6-09307b522493",
    name = "classes"
)
public class PlayerDungeonClassesCommand extends Command {

    protected PlayerDungeonClassesCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) {
        return Mono.empty();
    }

}
