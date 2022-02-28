package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "086d67ba-eba9-40db-958f-2f6759ae3b70",
    name = "slayers"
)
public class PlayerSlayersCommand extends Command {

    protected PlayerSlayersCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) {
        return Mono.empty();
    }

}
