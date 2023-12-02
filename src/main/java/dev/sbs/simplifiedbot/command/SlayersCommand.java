package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.context.interaction.deferrable.application.SlashCommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("086d67ba-eba9-40db-958f-2f6759ae3b70")
public class SlayersCommand extends SkyBlockUserCommand {

    protected SlayersCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull SlashCommandContext commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return commandContext.reply(
            Response.builder()
                .isInteractable()
                .replyMention()
                .withTimeToLive(30)
                .withPages(PlayerCommand.buildPages(skyBlockUser))
                .withDefaultPage("slayers")
                .build()
        );
    }

}
