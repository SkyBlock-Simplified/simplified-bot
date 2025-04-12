package dev.sbs.simplifiedbot.command.embed;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandStructure;
import dev.sbs.discordapi.command.SlashCommand;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandStructure(
    parent = "embed",
    name = "create"
)
public class EmbedCreateCommand extends SlashCommand {

    protected EmbedCreateCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) {
        return Mono.empty();
    }

}
