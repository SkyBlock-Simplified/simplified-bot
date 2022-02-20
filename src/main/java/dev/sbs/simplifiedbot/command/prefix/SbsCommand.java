package dev.sbs.simplifiedbot.command.prefix;

import dev.sbs.discordapi.command.PrefixCommand;
import dev.sbs.discordapi.command.UserPermission;
import dev.sbs.discordapi.command.data.CommandInfo;
import discord4j.rest.util.Permission;

@CommandInfo(
    id = "13543409-e862-4c5e-9af4-e37f38107efa",
    name = "beta", // TODO
    permissions = {
        Permission.VIEW_CHANNEL,
        Permission.SEND_MESSAGES,
        Permission.ADD_REACTIONS,
        Permission.USE_EXTERNAL_EMOJIS,
        Permission.MANAGE_MESSAGES,
    },
    userPermissions = { UserPermission.MAIN_SERVER_ADMIN }
)
public class SbsCommand extends PrefixCommand {

}
