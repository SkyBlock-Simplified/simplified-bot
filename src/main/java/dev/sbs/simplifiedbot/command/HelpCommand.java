package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.command_data.command_categories.CommandCategoryModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.interaction.deferrable.application.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.structure.Author;
import dev.sbs.discordapi.response.embed.structure.Field;
import dev.sbs.discordapi.response.embed.structure.Footer;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.util.SqlSlashCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;

@CommandId("e54b45ec-e47d-4784-bac4-72c4908ba87d")
public class HelpCommand extends SqlSlashCommand {

    protected HelpCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return commandContext.reply(
            Response.builder()
                .withTimeToLive(60)
                .replyMention()
                .isInteractable()
                .isEphemeral()
                .withPages(
                    Page.builder()
                        .withEmbeds(
                            Embed.builder()
                                .withAuthor(
                                    Author.builder()
                                        .withName("Help")
                                        .withIconUrl(getEmoji("STATUS_INFO").map(Emoji::getUrl))
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
                                                    .withIconUrl(getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                                    .build()
                                            )
                                            .withTitle("Category :: %s", commandCategory.getName())
                                            .withDescription(commandCategory.getDescription())
                                            .withColor(Color.DARK_GRAY)
                                            .withFields(
                                                this.getDiscordBot()
                                                    .getCommandRegistrar()
                                                    .getSlashCommands()
                                                    .stream()
                                                    .filter(command -> command.getCategory()
                                                        .map(category -> category.getName().equals(commandCategory.getKey()))
                                                        .orElse(false)
                                                    )
                                                    .map(command -> Field.builder()
                                                        .withEmoji(command.getEmoji())
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
                                            .getCommandRegistrar()
                                            .getSlashCommands()
                                            .stream()
                                            .filter(command -> command.getCategory()
                                                .map(category -> category.getName().equals(commandCategory.getKey()))
                                                .orElse(false)
                                            )
                                            .map(command -> Page.builder()
                                                .withOption(
                                                    SelectMenu.Option.builder()
                                                        .withEmoji(command.getEmoji())
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
            Parameter.builder("command", "The command you want help with.", Parameter.Type.WORD)
                .build()
        );
    }

}
