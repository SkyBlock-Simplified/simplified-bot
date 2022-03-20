package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.command_categories.CommandCategoryModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.discordapi.DiscordBot;
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
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;

@CommandInfo(
    id = "e54b45ec-e47d-4784-bac4-72c4908ba87d",
    name = "help"
)
public class HelpCommand extends Command {

    protected HelpCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {
        return commandContext.reply(
            Response.builder()
                .withReference(commandContext)
                .withTimeToLive(60)
                .replyMention()
                .isInteractable()
                .isEphemeral()
                .withPages(
                    Page.builder()
                        .withEmbeds(
                            Embed.builder()
                                .withAuthor("Help", getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                .withTitle("Categories")
                                .withDescription("Select a category in the select menu to view a list of commands.")
                                .withTimestamp(Instant.now())
                                .withColor(Color.DARK_GRAY)
                                .withFields(
                                    SimplifiedApi.getRepositoryOf(CommandCategoryModel.class)
                                        .stream()
                                        .map(category -> Field.builder()
                                            .withEmoji(Emoji.of(category.getEmoji()))
                                            .withName(category.getName())
                                            .withValue(category.getDescription())
                                            .isInline()
                                            .build()
                                        )
                                        .collect(Concurrent.toList())
                                )
                                .build()
                        )
                        .withPages(
                            SimplifiedApi.getRepositoryOf(CommandCategoryModel.class)
                                .stream()
                                .map(commandCategory -> Page.builder()
                                    .withOption(
                                        SelectMenu.Option.builder()
                                            .withEmoji(Emoji.of(commandCategory.getEmoji()))
                                            .withLabel(commandCategory.getName())
                                            .withDescription(commandCategory.getDescription())
                                            .withValue(commandCategory.getName())
                                            .build()
                                    )
                                    .withEmbeds(
                                        Embed.builder()
                                            .withAuthor("Help", getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                            .withTitle("Category :: {0}", commandCategory.getName())
                                            .withDescription(commandCategory.getDescription())
                                            .withTimestamp(Instant.now())
                                            .withColor(Color.DARK_GRAY)
                                            .withFields(
                                                this.getDiscordBot()
                                                    .getRootCommandRelationship()
                                                    .getSubCommands()
                                                    .stream()
                                                    .filter(relationship -> relationship.getInstance()
                                                        .getCategory()
                                                        .map(commandCategory::equals)
                                                        .orElse(false)
                                                    )
                                                    .map(relationship -> Field.builder()
                                                        .withEmoji(relationship.getInstance().getEmoji())
                                                        .withName(relationship.getCommandInfo().name())
                                                        .withValue(relationship.getInstance().getDescription())
                                                        .isInline()
                                                        .build()
                                                    )
                                                    .collect(Concurrent.toList())
                                            )
                                            .build()
                                    )
                                    .withPages(
                                        this.getDiscordBot()
                                            .getRootCommandRelationship()
                                            .getSubCommands()
                                            .stream()
                                            .filter(relationship -> relationship.getInstance()
                                                .getCategory()
                                                .map(commandCategory::equals)
                                                .orElse(false)
                                            )
                                            .map(relationship -> Page.builder()
                                                .withOption(
                                                    SelectMenu.Option.builder()
                                                        .withEmoji(relationship.getInstance().getEmoji())
                                                        .withLabel(relationship.getCommandInfo().name())
                                                        .withDescription(relationship.getInstance().getDescription())
                                                        .withValue(relationship.getCommandInfo().name())
                                                        .build()
                                                )
                                                .withEmbeds(relationship.createHelpEmbed(commandContext.isSlashCommand()))
                                                .build()
                                            )
                                            .collect(Concurrent.toList())
                                    )
                                    .build()
                                )
                                .collect(Concurrent.toList())
                        )
                        .build()
                )
                .build()
        );
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            new Parameter(
                "command",
                "The command you want help with.",
                Parameter.Type.WORD,
                false
            )
        );
    }

}
