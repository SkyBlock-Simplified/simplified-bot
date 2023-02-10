package dev.sbs.simplifiedbot.command.group.reputation;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation.GuildReputationModel;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation.GuildReputationSqlModel;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation_types.GuildReputationTypeModel;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation_types.GuildReputationTypeSqlModel;
import dev.sbs.api.data.sql.SqlRepository;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.ReputationCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Optional;

@CommandInfo(
    id = "6f97994d-a09b-45f2-9275-66d5028d5b39",
    name = "give",
    parent = ReputationCommand.class
)
public class RepGiveCommand extends Command {

    protected RepGiveCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {
        final long submitterDiscordId = commandContext.getInteractUserId().asLong();
        final long receiverDiscordId = Long.parseLong(commandContext.getArgument("user").getValue().orElseThrow());
        String reputationType = commandContext.getArgument("type").getValue().orElseThrow();
        String reason = commandContext.getArgument("reason").getValue().orElseThrow();
        Optional<Member> receivingMember = commandContext.getGuild()
            .flatMap(guild -> guild.getMemberById(Snowflake.of(receiverDiscordId)))
            .blockOptional();

        // Check if member is in current guild
        if (receivingMember.isEmpty())
            return commandContext.reply(genericResponse("That user is no longer in the server!", Color.RED));

        // Prevent Self Reputation
        if (submitterDiscordId == receiverDiscordId)
            return commandContext.reply(genericResponse("You cannot give yourself reputation!", Color.RED));

        Optional<GuildReputationTypeSqlModel> reputationTypeSqlModel = SimplifiedApi.getRepositoryOf(GuildReputationTypeSqlModel.class)
            .findFirst(GuildReputationTypeModel::getKey, reputationType.toUpperCase());

        // Check Reputation Type
        if (reputationTypeSqlModel.isEmpty())
            return commandContext.reply(genericResponse(FormatUtil.format("The provided reputation type ''{0}'' is invalid!", reputationType), Color.RED));

        long lastReceivedId = SimplifiedApi.getRepositoryOf(GuildReputationModel.class)
            .findLast(GuildReputationModel::getSubmitterDiscordId, submitterDiscordId)
            .map(GuildReputationModel::getReceiverDiscordId)
            .orElse(0L);

        // Prevent Continuous Reputation
        if (lastReceivedId == receiverDiscordId)
            return commandContext.reply(genericResponse("You have recently given rep to this user!", Color.RED));

        // Create New Reputation
        GuildReputationSqlModel entry = new GuildReputationSqlModel();
        entry.setSubmitterDiscordId(submitterDiscordId);
        entry.setReceiverDiscordId(receiverDiscordId);
        entry.setType(reputationTypeSqlModel.get());
        entry.setReason(reason);
        ((SqlRepository<GuildReputationSqlModel>) SimplifiedApi.getRepositoryOf(GuildReputationSqlModel.class)).save(entry);

        return commandContext.reply(
            genericResponse(FormatUtil.format(
                """
                    You have given +1 {0} reputation to {1}
                    Reason: {2}""",
                WordUtil.capitalizeFully(reputationType.replace("_", " ")),
                receivingMember.get().getMention(),
                reason
            ), Color.YELLOW)
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
                .build(),
            Parameter.builder("type", "Type of the given Rep", Parameter.Type.WORD)
                .isRequired()
                .withChoices(
                    SimplifiedApi.getRepositoryOf(GuildReputationTypeModel.class).findAll()
                        .stream().map(reputationType -> Pair.of(reputationType.getName(), reputationType.getKey().toLowerCase()))
                        .collect(Concurrent.toLinkedMap())
                )
                .build(),
            Parameter.builder("reason", "Reason for the Rep", Parameter.Type.TEXT)
                .isRequired()
                .build()
        );
    }

}
