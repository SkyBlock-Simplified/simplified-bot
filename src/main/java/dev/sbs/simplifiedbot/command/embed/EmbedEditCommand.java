package dev.sbs.simplifiedbot.command.embed;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.context.deferrable.application.SlashCommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.util.SqlSlashCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("0887bfa5-a8e8-401a-86c3-034362c0788a")
public class EmbedEditCommand extends SqlSlashCommand {

    protected EmbedEditCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @NotNull
    @Override
    protected Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return Mono.empty();
    }

}
