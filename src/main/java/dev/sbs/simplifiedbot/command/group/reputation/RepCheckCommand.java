package dev.sbs.simplifiedbot.command.group.reputation;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation.GuildReputationModel;
import dev.sbs.api.data.model.discord.guild_data.guild_reputation_types.GuildReputationTypeModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.data.tuple.Pair;
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
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Map;

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

        long userId = Long.parseLong(commandContext.getArgument("user").getValue().orElse("307984861166043137"));
        String userName = commandContext.getGuild()
            .flatMap(guild -> guild.getMemberById(Snowflake.of(userId)))
            .map(user -> user.getUsername() + "#" + user.getDiscriminator())
            .block();

        ConcurrentList<GuildReputationTypeModel> reputationTypes = SimplifiedApi.getRepositoryOf(GuildReputationTypeModel.class).findAll();
        Map<GuildReputationTypeModel, Integer> reputationAmounts = reputationTypes.stream().map(reputationType -> Pair.of(reputationType,
            SimplifiedApi.getRepositoryOf(GuildReputationModel.class).findAll(GuildReputationModel::getReceiverDiscordId, userId)
                .findAll(GuildReputationModel::getType, reputationType).size()))
            .collect(Concurrent.toMap());

        ConcurrentList<Field> typeFields = reputationTypes.stream().map(reputationType -> Field.builder()
            .withName(reputationType.getName())
            .withValue(reputationAmounts.get(reputationType).toString())
            .isInline()
            .build())
            .collect(Concurrent.toList());

        while (typeFields.size() % 3 != 0) {
            typeFields.add(Field.empty(true));
        }

        return commandContext.reply(
            Response.builder()
                .withPages(
                    Page.builder()
                        .withEmbeds(
                            Embed.builder()
                                .withAuthor("Reputation")
                                .withColor(Color.YELLOW)
                                .withTitle(userName)
                                .withField("Total", String.valueOf(reputationAmounts.values().stream().reduce(0, Integer::sum)), true)
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

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder("user", "Discord User", Parameter.Type.USER)
                .isRequired()
                .build()
        );
    }
}
