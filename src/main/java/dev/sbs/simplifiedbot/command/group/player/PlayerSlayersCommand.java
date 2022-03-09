package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "086d67ba-eba9-40db-958f-2f6759ae3b70",
    name = "slayers"
)
public class PlayerSlayersCommand extends SkyBlockUserCommand {

    protected PlayerSlayersCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return Mono.empty();
    }

}
