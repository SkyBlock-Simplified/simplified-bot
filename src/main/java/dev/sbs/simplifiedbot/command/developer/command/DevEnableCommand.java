package dev.sbs.simplifiedbot.command.developer.command;

import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.interaction.deferrable.application.slash.SlashCommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.util.SqlSlashCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("5f279de3-6cf2-468e-9ec1-b208637efc8c")
public class DevEnableCommand extends SqlSlashCommand {

    protected DevEnableCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return Mono.empty();
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder("command", "The command to enable.", Parameter.Type.WORD)
                .isRequired()
                .build()
        );
    }

}
