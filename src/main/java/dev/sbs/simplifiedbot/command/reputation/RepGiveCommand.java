package dev.sbs.simplifiedbot.command.reputation;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation.GuildReputationModel;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation.GuildReputationSqlModel;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation_types.GuildReputationTypeModel;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation_types.GuildReputationTypeSqlModel;
import dev.sbs.api.data.sql.SqlRepository;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.data.tuple.pair.Pair;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.command.parameter.Argument;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.deferrable.application.SlashCommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.util.SqlSlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Optional;

@CommandId("6f97994d-a09b-45f2-9275-66d5028d5b39")
public class RepGiveCommand extends SqlSlashCommand {

    protected RepGiveCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        final long submitterDiscordId = commandContext.getInteractUserId().asLong();
        final long receiverDiscordId = commandContext.getArgument("user").map(Argument::asLong).orElseThrow();
        String reputationType = commandContext.getArgument("type").map(Argument::asString).orElseThrow();
        String reason = commandContext.getArgument("reason").map(Argument::asString).orElseThrow();
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
            return commandContext.reply(genericResponse(String.format("The provided reputation type '%s' is invalid!", reputationType), Color.RED));

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
            genericResponse(String.format(
                """
                    You have given +1 %s reputation to %s
                    Reason: %s""",
                StringUtil.capitalizeFully(reputationType.replace("_", " ")),
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
            Parameter.builder()
                .withName("user")
                .withDescription("SkyBlock Profile Name")
                .withType(Parameter.Type.USER)
                .isRequired()
                .build(),
            Parameter.builder()
                .withName("type")
                .withDescription("Type of the given Rep")
                .withType(Parameter.Type.WORD)
                .isRequired()
                .withChoices(
                    SimplifiedApi.getRepositoryOf(GuildReputationTypeModel.class)
                        .stream()
                        .map(reputationType -> Pair.of(reputationType.getName(), reputationType.getKey().toLowerCase()))
                        .collect(Concurrent.toWeakLinkedMap())
                )
                .build(),
            Parameter.builder()
                .withName("reason")
                .withDescription("Reason for the Rep")
                .withType(Parameter.Type.TEXT)
                .isRequired()
                .build()
        );
    }

}
