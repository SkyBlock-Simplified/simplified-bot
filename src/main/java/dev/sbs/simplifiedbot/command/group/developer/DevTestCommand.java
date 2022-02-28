package dev.sbs.simplifiedbot.command.group.developer;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.action.Button;
import dev.sbs.discordapi.response.component.action.SelectMenu;
import dev.sbs.discordapi.response.component.layout.ActionRow;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "75f1762a-4672-48db-83d8-86d953645d08",
    name = "test",
    parent = DevCommand.class
)
public class DevTestCommand extends Command {

    protected DevTestCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) throws DiscordException {
        return commandContext.reply(
            Response.builder()
                .withContent("test command")
                .withReference(commandContext)
                .replyMention()
                .withReactions(Emoji.of("\uD83D\uDC80", reactionContext -> {
                    reactionContext.removeUserReaction();
                    reactionContext.edit(responseBuilder -> responseBuilder.withContent("reaction: " + reactionContext.getEmoji().asFormat()));
                }))
                .withEmbeds(
                    Embed.builder()
                        .withDescription("[Is this google?](https://google.com/)")
                        .build()
                )
                .withComponents(
                    ActionRow.of(
                        Button.builder()
                            .withStyle(Button.Style.PRIMARY)
                            .withEmoji(Emoji.of("\uD83C\uDF85"))
                            .withLabel("Santa")
                            .onInteract(buttonContext -> buttonContext.edit(responseBuilder -> responseBuilder.withContent("santa!")))
                            .build(),
                        Button.builder()
                            .withStyle(Button.Style.SECONDARY)
                            .withEmoji(Emoji.of("\uD83D\uDC31"))
                            .withLabel("Cat")
                            .onInteract(buttonContext -> buttonContext.edit(responseBuilder -> responseBuilder.withContent("cat!")))
                            .build(),
                        Button.builder()
                            .withStyle(Button.Style.LINK)
                            .withUrl("https://google.com/")
                            .withLabel("Google")
                            .isPreserved()
                            .build()
                    ),
                    ActionRow.of(
                        SelectMenu.builder()
                            .withPlaceholder("Derpy menu")
                            .placeholderUsesSelectedOption()
                            .withOptions(
                                SelectMenu.Option.builder()
                                    .withLabel("Neigh")
                                    .withValue("value 1")
                                    .withEmoji(getEmoji("SKYBLOCK_ICON_HORSE"))
                                    .onInteract(optionContext -> optionContext.edit(responseBuilder -> responseBuilder.withContent(optionContext.getOption().getValue())))
                                    .build(),
                                SelectMenu.Option.builder()
                                    .withLabel("Buni")
                                    .withValue("value 2")
                                    .withDescription("Looking for ores!")
                                    .withEmoji(Emoji.of(669279331875946506L, "Buni", true))
                                    .onInteract(optionContext -> optionContext.edit(responseBuilder -> responseBuilder.withContent(optionContext.getOption().getValue())))
                                    .build(),
                                SelectMenu.Option.builder()
                                    .withLabel("Santa Claus")
                                    .withValue("value 3")
                                    .withEmoji(Emoji.of("\uD83C\uDF85"))
                                    .onInteract(optionContext -> optionContext.edit(responseBuilder -> responseBuilder.withContent(optionContext.getOption().getValue())))
                                    .build()
                            )
                            .build()
                    )
                )
                .build()
            );
    }

}
