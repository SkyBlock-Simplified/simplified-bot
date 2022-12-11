package dev.sbs.simplifiedbot.command.group.embed;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.menu.Menu;
import dev.sbs.discordapi.response.menu.item.MenuItem;
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
        Menu.builder()
            .withItems(
                MenuItem.stringBuilder()
                    .build()
            )
            .build();

        // TODO: Add support to show Field somewhere else,
        //  possibly by building an alternate MenuItem like
        //  TitleMenuItem, HeaderMenuItem, FooterMenuItem, DescriptionMenuItem, etc.
        //  The menu builder should accept those separately from .withItems

        return Mono.empty();
    }

}
