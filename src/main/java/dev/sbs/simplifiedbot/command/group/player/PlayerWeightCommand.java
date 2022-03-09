package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "93d1546e-5522-4eed-95d5-cee418e1a2c4",
    name = "weight"
)
public class PlayerWeightCommand extends SkyBlockUserCommand {

    protected PlayerWeightCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return Mono.empty();
    }

}
