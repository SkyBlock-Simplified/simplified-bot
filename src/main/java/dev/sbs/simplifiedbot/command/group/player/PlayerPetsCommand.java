package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.UserPermission;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;

@CommandInfo(
    id = "ec1c37e2-ea99-4da2-ba04-8eecc368cda9",
    name = "pets",
    userPermissions = { UserPermission.BOT_OWNER }
)
public class PlayerPetsCommand extends Command {

    protected PlayerPetsCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {

    }

}
