package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "93d1546e-5522-4eed-95d5-cee418e1a2c4",
    name = "weight"
)
public class PlayerWeightCommand extends Command {

    protected PlayerWeightCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) {
        return Mono.empty();
    }

}
