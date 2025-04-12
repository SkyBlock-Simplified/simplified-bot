package dev.sbs.simplifiedbot.command.developer;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandStructure;
import dev.sbs.discordapi.command.SlashCommand;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.exception.DiscordException;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandStructure(
    parent = "dev",
    name = "activity"
)
public class DevActivityCommand extends SlashCommand {

    protected DevActivityCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return Mono.empty();
    }

}
