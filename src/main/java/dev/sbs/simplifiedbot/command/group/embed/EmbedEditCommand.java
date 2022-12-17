package dev.sbs.simplifiedbot.command.group.embed;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.simplifiedbot.command.EmbedCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "0887bfa5-a8e8-401a-86c3-034362c0788a",
    name = "edit",
    parent = EmbedCommand.class
)
public class EmbedEditCommand extends Command {

    protected EmbedEditCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        return Mono.empty();
    }

}
