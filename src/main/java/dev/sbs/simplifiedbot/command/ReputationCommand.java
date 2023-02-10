package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.ParentCommand;
import dev.sbs.discordapi.command.data.CommandInfo;
import org.jetbrains.annotations.NotNull;

@CommandInfo(
    id = "0779f314-0262-48a9-9d5a-24b500f422a0",
    name = "rep"
)
public class ReputationCommand extends ParentCommand {
    protected ReputationCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }
}
