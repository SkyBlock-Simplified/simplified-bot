package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Category;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Response;

@CommandInfo(
    name = "missing",
    category = Category.PLAYER,
    description = "Lookup missing accessories."
)
public class MissingCommand extends Command {

    protected MissingCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {
        commandContext.reply(
            Response.builder()
                .withContent("missing command")
                .withReference(commandContext)
                .build()
        );
    }

}
