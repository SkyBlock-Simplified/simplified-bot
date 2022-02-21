package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.UserPermission;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;

@CommandInfo(
    id = "df6db078-5f43-41f8-9439-40f21b3ddc18",
    name = "networth",
    userPermissions = { UserPermission.BOT_OWNER }
)
public class PlayerNetworthCommand extends Command {

    protected PlayerNetworthCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {

    }

}
