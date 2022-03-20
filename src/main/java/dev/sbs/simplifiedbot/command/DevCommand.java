package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.UserPermission;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "a48552da-56bb-4262-b48f-05ad3dee5ff6",
    name = "dev",
    userPermissions = { UserPermission.DEVELOPER }
)
public class DevCommand extends Command {

    protected DevCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        return Mono.empty();
    }

}
