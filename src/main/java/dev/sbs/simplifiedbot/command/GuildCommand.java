package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Response;

import java.util.regex.Pattern;

@CommandInfo(
    name = "guild",
    description = "Lookup a Hypixel Guild."
)
public class GuildCommand extends Command {

    private static final Pattern MOJANG_NAME = Pattern.compile("[\\w]{3,16}");

    protected GuildCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {
        commandContext.reply(
            Response.builder()
                .withContent("guild command")
                .withReference(commandContext)
                .build()
        );
    }

}
