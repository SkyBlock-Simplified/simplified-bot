package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Response;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@CommandInfo(
    id = "b04d133d-3532-447b-8782-37d1036f3957",
    name = "guild"
)
public class GuildCommand extends Command {

    private static final Pattern MOJANG_NAME = Pattern.compile("[\\w]{3,16}");

    protected GuildCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) {
        return commandContext.reply(
            Response.builder()
                .withContent("guild command")
                .withReference(commandContext)
                .build()
        );
    }

}
