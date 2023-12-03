package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.context.interaction.deferrable.application.SlashCommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("9171bc6d-fe0b-45e0-bf8f-bdbe87ccf064")
public class OptimizerCommand extends SkyBlockUserCommand {

    protected OptimizerCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull SlashCommandContext commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return commandContext.reply(
            Response.builder()
                .withPages(
                    Page.builder()
                        .withContent("optimizer command")
                        .build()
                )
                .build()
        );
    }

}
