package dev.sbs.simplifiedbot.command;

import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Category;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.util.exception.DiscordException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;

@CommandInfo(
    name = "help"
)
public class HelpCommand extends Command {

    protected HelpCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) throws DiscordException {
        commandContext.reply(
            Response.builder()
                .replyMention()
                .withReference(commandContext)
                .isInteractable()
                .withTimeToLive(60)
                .withEmbeds(
                    Embed.builder()
                        .withAuthor("Help", Emoji.of(929250313821638666L, "status_info").getUrl())
                        .withTitle("Categories")
                        .withDescription("Select a category in the select menu to view a list of commands.")
                        .withTimestamp(Instant.now())
                        .withColor(Color.DARK_GRAY)
                        .withFields(
                            Arrays.stream(Category.values())
                                .filter(category -> category != Category.UNCATEGORIZED)
                                .map(category -> Field.of(
                                    FormatUtil.format("{0}{1}", category.getEmoji().map(emoji -> emoji.asFormat() + " ").orElse(""), category.getName()),
                                    category.getDescription(),
                                    true
                                ))
                                .collect(Concurrent.toList())
                        )
                        .build()
                )
                .withPages(
                    Arrays.stream(Category.values())
                        .filter(category -> category != Category.UNCATEGORIZED)
                        .map(category -> Page.create()
                            .withOption(
                                SelectMenu.Option.builder()
                                    .withEmoji(category.getEmoji())
                                    .withLabel(category.getName())
                                    .withDescription(category.getDescription())
                                    .withValue(category.name())
                                    .build()
                            )
                            .withEmbeds(
                                Embed.builder()
                                    .withAuthor("Help", Emoji.of(929250313821638666L, "status_info").getUrl())
                                    .withTitle("Category :: {0}", category.getName())
                                    .withDescription(category.getDescription())
                                    .withTimestamp(Instant.now())
                                    .withColor(Color.DARK_GRAY)
                                    .withFields(
                                        this.getDiscordBot()
                                            .getRootCommandRelationship()
                                            .getSubCommands()
                                            .stream()
                                            .filter(relationship -> relationship.getCommandInfo().category() == category)
                                            .map(relationship -> Field.of(
                                                FormatUtil.format(
                                                    "{0}{1}",
                                                    relationship.getInstance()
                                                        .getEmoji()
                                                        .map(emoji -> emoji.asFormat() + " ")
                                                        .orElse(""),
                                                    relationship.getCommandInfo().name()
                                                ),
                                                relationship.getInstance().getDescription(),
                                                true
                                            ))
                                            .collect(Concurrent.toList())
                                    )
                                    .build()
                            )
                            .withPages(
                                this.getDiscordBot()
                                    .getRootCommandRelationship()
                                    .getSubCommands()
                                    .stream()
                                    .filter(relationship -> relationship.getCommandInfo().category() == category)
                                    .map(relationship -> Page.create()
                                        .withOption(
                                            SelectMenu.Option.builder()
                                                .withEmoji(relationship.getInstance().getEmoji())
                                                .withLabel(relationship.getCommandInfo().name())
                                                .withDescription(relationship.getInstance().getDescription())
                                                .withValue(relationship.getCommandInfo().name())
                                                .build()
                                        )
                                        .withEmbeds(Command.createHelpEmbed(relationship))
                                        .build()
                                    )
                                    .collect(Concurrent.toList())
                            )
                            .build()
                        )
                        .collect(Concurrent.toList())
                )
                .build()
        );
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            new Parameter(
                "command",
                "The command you want the help menu for.",
                Parameter.Type.TEXT,
                false
            )
        );
    }

}
