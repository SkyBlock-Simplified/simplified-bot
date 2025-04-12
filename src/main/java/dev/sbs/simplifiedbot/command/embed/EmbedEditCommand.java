package dev.sbs.simplifiedbot.command.embed;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandStructure;
import dev.sbs.discordapi.command.SlashCommand;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.exception.DiscordException;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;


@CommandStructure(
    parent = "embed",
    name = "edit"
)
public class EmbedEditCommand extends SlashCommand {

    protected EmbedEditCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @NotNull
    @Override
    protected Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return Mono.empty();
    }

}
