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

@CommandId("a7e43d59-38f2-41d9-ba0f-4f2664b212f7")
public class PlayerAccessoriesCommand extends SkyBlockUserCommand {

    protected PlayerAccessoriesCommand(@NotNull DiscordBot discordBot) {
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
                .withDefaultPage("accessories")
                .build()
        );
    }

}
