package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Structure;
import dev.sbs.discordapi.context.command.SlashCommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@Structure(
    name = "jacobs",
    description = "View a players Jacobs' farming progress"
)
public class JacobsCommand extends SkyBlockUserCommand {

    protected JacobsCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull SlashCommandContext commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return commandContext.reply(
            Response.builder()
                .withTimeToLive(30)
                .withPages(buildPages(skyBlockUser))
                .withDefaultPage("jacobs_farming")
                .build()
        );
    }

}
