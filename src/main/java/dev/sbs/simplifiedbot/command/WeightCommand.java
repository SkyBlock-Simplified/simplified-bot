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
    name = "weight",
    description = "Lookup a players weight"
)
public class WeightCommand extends SkyBlockUserCommand {

    protected WeightCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull SlashCommandContext commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return commandContext.reply(
            Response.builder()
                .withTimeToLive(30)
                .withPages(buildPages(skyBlockUser))
                .withDefaultPage("weight")
                .build()
        );
    }

}
