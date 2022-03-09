package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "cc65f062-45f8-44c0-9635-84359e3ea246",
    name = "dungeons"
)
public class PlayerDungeonsCommand extends SkyBlockUserCommand {

    protected PlayerDungeonsCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return Mono.empty();
    }

}
