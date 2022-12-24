package dev.sbs.simplifiedbot.command.group.embed;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.item.FooterItem;
import dev.sbs.simplifiedbot.command.EmbedCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "5c0512d2-49c6-45fb-a95c-beb25d541e5f",
    name = "edit",
    parent = EmbedCommand.class
)
public class EmbedCreateCommand extends Command {

    protected EmbedCreateCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        Page.builder()
            .withItems(
                FooterItem.builder()
                    .isEditable()
                    .withName("yee haw")
                    .build()
            )
            .build();

        return Mono.empty();
    }

}
