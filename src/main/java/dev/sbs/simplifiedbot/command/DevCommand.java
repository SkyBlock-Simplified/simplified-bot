package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.ParentCommand;
import dev.sbs.discordapi.command.UserPermission;
import dev.sbs.discordapi.command.data.CommandInfo;
import org.jetbrains.annotations.NotNull;

@CommandInfo(
    id = "a48552da-56bb-4262-b48f-05ad3dee5ff6",
    name = "dev",
    userPermissions = { UserPermission.DEVELOPER }
)
public class DevCommand extends ParentCommand {

    protected DevCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

}
