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
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;

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
        String receiverDiscordId = commandContext.getArgument("user").getValue().orElse("307984861166043137");
        String typeString = commandContext.getArgument("type").getValue().orElse("crafting");
        GuildReputationTypeSqlModel type = SimplifiedApi.getRepositoryOf(GuildReputationTypeSqlModel.class)
            .findFirst(GuildReputationTypeModel::getKey, typeString.toUpperCase()).orElseThrow();
        String submitterDiscordId = commandContext.getInteractUserId().asString();
        String reason = commandContext.getArgument("reason").getValue().orElse("");

        if (submitterDiscordId.equals(receiverDiscordId)) {
            return commandContext.reply(
                genericResponse("You cannot give yourself Reputation!", Color.RED)
            );
        }


        String lastReportedId = String.valueOf(SimplifiedApi.getRepositoryOf(GuildReputationModel.class)
            .findLast(GuildReputationModel::getSubmitterDiscordId, submitterDiscordId)
            .map(GuildReputationModel::getReceiverDiscordId)
            .orElse(0L));

        if (lastReportedId.equals(receiverDiscordId)) {
            return commandContext.reply(
                genericResponse("You recently repped this user!", Color.RED)
            );
        }

        GuildReputationSqlModel entry = new GuildReputationSqlModel();
        entry.setSubmitterDiscordId(Long.valueOf(submitterDiscordId));
        entry.setReceiverDiscordId(Long.valueOf(receiverDiscordId));
        entry.setType(type);
        entry.setReason(reason);
        ((SqlRepository<GuildReputationSqlModel>) SimplifiedApi.getRepositoryOf(GuildReputationSqlModel.class)).save(entry);

        return commandContext.reply(
            genericResponse("You have given +1 " + WordUtil.capitalizeFully(typeString) + " Reputation to  <@" + receiverDiscordId + ">"
                + "\nReason: " + reason, Color.YELLOW)
        );
    }

    private Response genericResponse(String description, Color color) {
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
