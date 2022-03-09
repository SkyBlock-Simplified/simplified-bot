package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "ec1c37e2-ea99-4da2-ba04-8eecc368cda9",
    name = "pets"
)
public class PlayerPetsCommand extends SkyBlockUserCommand {

    protected PlayerPetsCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return Mono.empty();
    }

}
