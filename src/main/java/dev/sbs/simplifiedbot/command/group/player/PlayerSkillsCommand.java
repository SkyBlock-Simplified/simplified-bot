package dev.sbs.simplifiedbot.command.group.player;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "938888f7-1950-484c-a1e2-73bd8df2871a",
    name = "skills"
)
public class PlayerSkillsCommand extends Command {

    protected PlayerSkillsCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) {
        return Mono.empty();
    }

}
