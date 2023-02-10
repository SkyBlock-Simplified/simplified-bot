package dev.sbs.simplifiedbot.command.group.reputation;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation.GuildReputationModel;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation_types.GuildReputationTypeModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.collection.search.function.SearchFunction;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.ListUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.ReputationCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Map;
import java.util.Optional;

@CommandInfo(
    id = "01cd83a4-cc84-4b76-9e16-dbdab43e80ff",
    name = "check",
    parent = ReputationCommand.class
)
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

        Map<GuildReputationTypeModel, Integer> receiverReputation = reputationTypes.stream()
            .map(reputationType -> Pair.of(
                reputationType,
                SimplifiedApi.getRepositoryOf(GuildReputationModel.class)
                    .findAll(
                        Pair.of(GuildReputationModel::getType, reputationType),
                        Pair.of(GuildReputationModel::getReceiverDiscordId, receiverDiscordId)
                    )
                    .size()
            ))
            .collect(Concurrent.toMap());

        ConcurrentList<Field> typeFields = reputationTypes.stream()
            .map(reputationType -> Field.builder()
                .withName(reputationType.getName())
                .withValue(receiverReputation.get(reputationType).toString())
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
                                .withField("Total", String.valueOf(receiverReputation.values().stream().reduce(0, Integer::sum)), true)
                                .withField("Unverified", "x", true)
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
