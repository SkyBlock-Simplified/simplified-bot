package dev.sbs.simplifiedbot.command.group.reputation;

import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.WordUtil;
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

@CommandInfo(
    id = "01cd83a4-cc84-4b76-9e16-dbdab43e80ff",
    name = "check",
    parent = ReputationCommand.class
)
public class RepCheckCommand extends Command {
    protected RepCheckCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    public static final ConcurrentList<String> reputationTypes = new ConcurrentList<>(
        "crafting",
        "services"
    );

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {

        String userId = commandContext.getArgument("user").getValue().orElse("307984861166043137");
        String userName = commandContext.getGuild()
            .flatMap(guild -> guild.getMemberById(Snowflake.of(userId)))
            .map( user -> user.getUsername() + "#" + user.getDiscriminator())
            .block();

        ConcurrentList<Field> typeFields = reputationTypes.stream().map(reputationType -> Field.builder()
            .withName(WordUtil.capitalizeFully(reputationType))
            .withValue("0")
            .isInline()
            .build())
            .collect(Concurrent.toList());

        for (int i = 0; i < typeFields.size() % 3; i++) {
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
                                .withField("Total", "0", true)
                                .withField("Unverified", "0", true)
                                .withEmptyField(true)
                                .withFields(typeFields)
                                .build()
                        )
                        .build()
                )
                .build()
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
                .build()
//            Parameter.builder("type", "Type of the given Rep", Parameter.Type.WORD)
//                .isRequired()
//                .withChoices(
//                    reputationTypes.stream().map(reputationType -> Pair.of(WordUtil.capitalizeFully(reputationType.replace("_", " ")), reputationType))
//                        .collect(Concurrent.toLinkedMap())
//                )
//                .build()
        );
    }
}
