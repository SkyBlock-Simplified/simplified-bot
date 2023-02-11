package dev.sbs.simplifiedbot.command.reputation;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation.GuildReputationModel;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation_types.GuildReputationTypeModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.collection.search.function.SearchFunction;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.ListUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandId;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.util.exception.DiscordException;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;

@CommandId("01cd83a4-cc84-4b76-9e16-dbdab43e80ff")
public class RepCheckCommand extends Command {

    protected RepCheckCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {
        final long receiverDiscordId = Long.parseLong(commandContext.getArgument("user").getValue().orElseThrow());
        Optional<Member> receivingMember = commandContext.getGuild()
            .flatMap(guild -> guild.getMemberById(Snowflake.of(receiverDiscordId)))
            .blockOptional();

        // Check if member is in current guild
        if (receivingMember.isEmpty())
            return commandContext.reply(genericResponse("That user is no longer in the server!", Color.RED));

        // Load Nickname (Tag) or Tag
        String userName = receivingMember.get()
            .getNickname()
            .map(nick -> FormatUtil.format(
                "{0} ({1})",
                nick,
                receivingMember.get().getTag()
            ))
            .orElse(receivingMember.get().getTag());

        // Get reputation types for current Guild
        ConcurrentList<GuildReputationTypeModel> reputationTypes = SimplifiedApi.getRepositoryOf(GuildReputationTypeModel.class)
            .findAll(SearchFunction.combine(GuildReputationTypeModel::getGuild, GuildModel::getGuildId), commandContext.getGuildId().orElseThrow());

        // Check if reputation types have been created
        if (ListUtil.isEmpty(reputationTypes))
            return commandContext.reply(genericResponse("No reputation types have been setup!", Color.RED));

        ConcurrentMap<GuildReputationTypeModel, ConcurrentList<GuildReputationModel>> receiverReputation = reputationTypes.stream()
            .map(reputationType -> Pair.of(
                reputationType,
                SimplifiedApi.getRepositoryOf(GuildReputationModel.class)
                    .findAll(
                        Pair.of(GuildReputationModel::getType, reputationType),
                        Pair.of(GuildReputationModel::getReceiverDiscordId, receiverDiscordId)
                    )
            ))
            .collect(Concurrent.toMap());

        ConcurrentList<Field> typeFields = reputationTypes.stream()
            .map(reputationType -> Field.builder()
                .withName(reputationType.getName())
                .withValue(String.valueOf(receiverReputation.get(reputationType).size()))
                .isInline()
                .build()
            )
            .collect(Concurrent.toList());

        while (typeFields.size() % 3 != 0)
            typeFields.add(Field.empty(true));

        return commandContext.reply(
            Response.builder()
                .withPages(
                    Page.builder()
                        .withEmbeds(
                            Embed.builder()
                                .withAuthor(userName, receivingMember.get().getAvatarUrl())
                                .withColor(Color.YELLOW)
                                .withTitle("Reputation")
                                .withDescription(
                                    """
                                        Total: All verified and unverified reputation a user has been given.
                                        Verified: The total reputation confirmed by the staff team.
                                        Unverified: The total reputation not yet confirmed by the staff team."""
                                )
                                .withField("Total", String.valueOf(receiverReputation.values().stream().mapToInt(ConcurrentList::size).sum()), true)
                                .withField("Verified", String.valueOf(
                                    receiverReputation.values()
                                        .stream()
                                        .flatMap(list -> list.stream().filter(guildReputationModel -> Objects.nonNull(guildReputationModel.getAssigneeDiscordId())))
                                        .count()
                                ), true)
                                .withField("Unverified", String.valueOf(
                                    receiverReputation.values()
                                        .stream()
                                        .flatMap(list -> list.stream().filter(guildReputationModel -> Objects.isNull(guildReputationModel.getAssigneeDiscordId())))
                                        .count()
                                ), true)
                                .withEmptyField(true)
                                .withFields(typeFields)
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

    private static Response genericResponse(String description, Color color) {
        return Response.builder()
            .withPages(
                Page.builder()
                    .withEmbeds(
                        Embed.builder()
                            .withTitle("Reputation")
                            .withDescription(description)
                            .withColor(color)
                            .build()
                    )
                    .build()
            )
            .build();
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder("user", "Discord User", Parameter.Type.USER)
                .isRequired()
                .build()
        );
    }

}
