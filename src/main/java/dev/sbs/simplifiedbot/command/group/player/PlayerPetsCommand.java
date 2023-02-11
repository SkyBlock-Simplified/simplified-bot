package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.data.CommandId;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.simplifiedbot.command.PlayerCommand;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("ec1c37e2-ea99-4da2-ba04-8eecc368cda9")
public class PlayerPetsCommand extends SkyBlockUserCommand {

    protected PlayerPetsCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return commandContext.reply(
            Response.builder()
                .isInteractable()
                .replyMention()
                .withReference(commandContext)
                .withTimeToLive(30)
                .withPages(PlayerCommand.buildPages(skyBlockUser))
                .withDefaultPage("pets")
                .build()
        );
    }

}
