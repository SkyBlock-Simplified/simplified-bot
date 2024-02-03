package dev.sbs.simplifiedbot.command.developer.command;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.util.SqlSlashCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("79dbe55c-22a5-41dc-9bb0-fc956e2390b3")
public class DevDisableCommand extends SqlSlashCommand {

    protected DevDisableCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return Mono.empty();
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder()
                .withName("command")
                .withDescription("The command to disable.")
                .withType(Parameter.Type.WORD)
                .isRequired()
                .build(),
            Parameter.builder()
                .withName("reason")
                .withDescription("The reason the command is being disabled.")
                .withType(Parameter.Type.TEXT)
                .build()
        );
    }

}
