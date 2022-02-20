package dev.sbs.simplifiedbot.command;

import dev.sbs.api.util.builder.string.StringBuilder;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.UserPermission;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Response;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationTeamData;
import discord4j.discordjson.json.ApplicationTeamMemberData;
import discord4j.discordjson.json.UserData;

@CommandInfo(
    id = "a48552da-56bb-4262-b48f-05ad3dee5ff6",
    name = "dev",
    userPermissions = { UserPermission.BOT_OWNER }
)
public class DeveloperCommand extends Command {

    protected DeveloperCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {
        ApplicationCommandInteractionEvent acie;
        ChatInputInteractionEvent ciie;

        this.getDiscordBot()
            .getClient()
            .getApplicationInfo()
            .blockOptional()
            .ifPresent(applicationInfoData -> {
                if (applicationInfoData.team().isPresent()) {
                    // Handle Team
                    StringBuilder teamMembers = new StringBuilder();
                    ApplicationTeamData applicationTeamData = applicationInfoData.team().get();
                    ConcurrentList<ApplicationTeamMemberData> members = Concurrent.newList(applicationTeamData.members());
                    members.inverse().forEach(applicationTeamMemberData -> {
                        UserData teamOwner = applicationTeamMemberData.user();
                        teamMembers.appendSeparator(", ");
                        teamMembers.append(teamOwner.username());
                    });

                    commandContext.reply(
                        Response.builder()
                            .withContent("Team Members: " + teamMembers.build())
                            .build()
                    );
                } else {
                    // Handle Owner
                    UserData botOwner = applicationInfoData.owner();
                    String x = "";
                }
            });
    }

}
