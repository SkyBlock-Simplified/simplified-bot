package dev.sbs.simplifiedbot.command.embed;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandId;
import dev.sbs.discordapi.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("0887bfa5-a8e8-401a-86c3-034362c0788a")
public class EmbedEditCommand extends Command {

    protected EmbedEditCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        return Mono.empty();
    }

}
