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
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;
import org.jetbrains.annotations.NotNull;
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
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {
        return commandContext.reply(
            Response.builder()
                .withReference(commandContext)
                .replyMention()
                .withTimeToLive(30)
                .withPages(
                    Page.builder()
                        .withContent("test command")
                        .withReactions(Emoji.of("\uD83D\uDC80", reactionContext -> reactionContext.removeUserReaction()
                            .then(reactionContext.edit(pageBuilder -> pageBuilder.withContent("reaction: " + reactionContext.getEmoji().asFormat()))))
                        )
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
                                    .onInteract(buttonContext -> buttonContext.edit(pageBuilder -> pageBuilder.withContent("santa!")))
                                    .build(),
                                Button.builder()
                                    .withStyle(Button.Style.SECONDARY)
                                    .withEmoji(Emoji.of("\uD83D\uDC31"))
                                    .withLabel("Cat")
                                    .onInteract(buttonContext -> buttonContext.edit(pageBuilder -> pageBuilder.withContent("cat!")))
                                    .build(),
                                Button.builder()
                                    .withStyle(Button.Style.DANGER)
                                    .withLabel("Danger!")
                                    .build(),
                                Button.builder()
                                    .withStyle(Button.Style.SUCCESS)
                                    .withLabel("Success!")
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
                                    .withPlaceholderUsesSelectedOption()
                                    .withOptions(
                                        SelectMenu.Option.builder()
                                            .withLabel("Neigh")
                                            .withValue("value 1")
                                            .withEmoji(getEmoji("SKYBLOCK_ICON_HORSE"))
                                            .onInteract(optionContext -> optionContext.edit(pageBuilder -> pageBuilder.withContent(optionContext.getOption().getValue())))
                                            .build(),
                                        SelectMenu.Option.builder()
                                            .withLabel("Buni")
                                            .withValue("value 2")
                                            .withDescription("Looking for ores!")
                                            .withEmoji(Emoji.of(769279331875946506L, "Buni", true))
                                            .onInteract(optionContext -> optionContext.edit(pageBuilder -> pageBuilder.withContent(optionContext.getOption().getValue())))
                                            .build(),
                                        SelectMenu.Option.builder()
                                            .withLabel("Yes sir!")
                                            .withValue("value 3")
                                            .withEmoji(Emoji.of(837805777187241985L, "linasalute"))
                                            .onInteract(optionContext -> optionContext.edit(pageBuilder -> pageBuilder.withContent(optionContext.getOption().getValue())))
                                            .build(),
                                        SelectMenu.Option.builder()
                                            .withLabel("I do nothing :)")
                                            .withValue("value 4")
                                            .withEmoji(Emoji.of(851662312925954068L, "goosewalk", true))
                                            .build()
                                    )
                                    .build()
                            )
                        )
                        .build()
                )
                .build()
            );
    }

}
