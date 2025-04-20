package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.DiscordCommand;
import dev.sbs.discordapi.command.Structure;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.exception.DiscordException;
import dev.sbs.discordapi.handler.EmojiHandler;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.structure.Author;
import dev.sbs.discordapi.response.embed.structure.Field;
import dev.sbs.discordapi.response.embed.structure.Footer;
import dev.sbs.discordapi.response.page.Page;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;

@Structure(
    name = "help"
)
public class HelpCommand extends DiscordCommand<SlashCommandContext> {

    protected HelpCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return commandContext.reply(
            Response.builder()
                .withTimeToLive(60)
                //.isInteractable()
                .isEphemeral()
                .withPages(
                    Page.builder()
                        .withEmbeds(
                            Embed.builder()
                                .withAuthor(
                                    Author.builder()
                                        .withName("Help")
                                        .withIconUrl(EmojiHandler.getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                        .build()
                                )
                                .withTitle("Categories")
                                .withDescription("Select a category in the select menu to view a list of commands.")
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
                                .withFooter(
                                    Footer.builder()
                                        .withTimestamp(Instant.now())
                                        .build()
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
                                            .withAuthor(
                                                Author.builder()
                                                    .withName("Help")
                                                    .withIconUrl(EmojiHandler.getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                                    .build()
                                            )
                                            .withTitle("Category :: %s", commandCategory.getName())
                                            .withDescription(commandCategory.getDescription())
                                            .withColor(Color.DARK_GRAY)
                                            .withFields(
                                                this.getDiscordBot()
                                                    .getCommandHandler()
                                                    .getSlashCommands()
                                                    .stream()
                                                    .filter(command -> command.getCategory()
                                                        .map(category -> category.getName().equals(commandCategory.getKey()))
                                                        .orElse(false)
                                                    )
                                                    .map(command -> Field.builder()
                                                        .withName(command.getName())
                                                        .withValue(command.getDescription())
                                                        .isInline()
                                                        .build()
                                                    )
                                                    .collect(Concurrent.toList())
                                            )
                                            .withFooter(
                                                Footer.builder()
                                                    .withTimestamp(Instant.now())
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .withPages(
                                        this.getDiscordBot()
                                            .getCommandHandler()
                                            .getSlashCommands()
                                            .stream()
                                            .filter(command -> command.getCategory()
                                                .map(category -> category.getName().equals(commandCategory.getKey()))
                                                .orElse(false)
                                            )
                                            .map(command -> Page.builder()
                                                .withOption(
                                                    SelectMenu.Option.builder()
                                                        .withLabel(command.getName())
                                                        .withDescription(command.getDescription())
                                                        .withValue(command.getName())
                                                        .build()
                                                )
                                                .withEmbeds(command.createHelpEmbed())
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
            Parameter.builder()
                .withName("command")
                .withDescription("The command you want help with.")
                .withType(Parameter.Type.WORD)
                .build()
        );
    }

}
