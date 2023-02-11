package dev.sbs.simplifiedbot.command.embed;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandId;
import dev.sbs.discordapi.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandId("5c0512d2-49c6-45fb-a95c-beb25d541e5f")
public class EmbedCreateCommand extends Command {

    protected EmbedCreateCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
//        Page.builder()
//            .withItems(
//                FooterItem.builder()
//                    .isEditable()
//                    .withName("yee haw") //TODO: yee haw
//                    .build()
//            )
//            .build();

        return Mono.empty();
    }

}
