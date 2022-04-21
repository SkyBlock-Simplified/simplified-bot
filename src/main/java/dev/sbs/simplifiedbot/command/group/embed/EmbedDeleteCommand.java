package dev.sbs.simplifiedbot.command.group.embed;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.simplifiedbot.command.EmbedCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "b88cc79c-f901-4fcc-92f3-d25a630cea44",
    name = "delete",
    parent = EmbedCommand.class
)
public class EmbedDeleteCommand extends Command {

    protected EmbedDeleteCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        return Mono.empty();
    }

}
