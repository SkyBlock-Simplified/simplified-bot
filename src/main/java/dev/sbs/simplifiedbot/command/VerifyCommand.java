package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Response;

@CommandInfo(
    name = "verify",
    description = "Link your Hypixel Account to your Discord Account."
)
public class VerifyCommand extends Command {

    protected VerifyCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {
        commandContext.reply(
            Response.builder()
                .withContent("verify command")
                .withReference(commandContext)
                .build()
        );
    }

}
