package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.UserPermission;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;

@CommandInfo(
    id = "cc65f062-45f8-44c0-9635-84359e3ea246",
    name = "dungeons",
    userPermissions = { UserPermission.BOT_OWNER }
)
public class PlayerDungeonsCommand extends Command {

    protected PlayerDungeonsCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {

    }

}
