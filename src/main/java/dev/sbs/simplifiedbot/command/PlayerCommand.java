package dev.sbs.simplifiedbot.command;

import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Category;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Response;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@CommandInfo(
    name = "player",
    category = Category.PLAYER,
    description = "Lookup a players SkyBlock profile.",
    aliases = { "check", "lookup", "auctions", "stats", "pets", "slayer", "dungeons?", "networth", "minions", "farming", "jacobs", "reputation" }
)
public class PlayerCommand extends Command {

    private static final Pattern MOJANG_NAME = Pattern.compile("[\\w]{3,16}");

    protected PlayerCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {
        //MojangProfileResponse response = SimplifiedApi.getWebApi(MojangData.class).getProfileFromUsername("CraftedFury");
        commandContext.reply(
            Response.builder()
                .withContent("player command")
                .withReference(commandContext)
                .build()
        );

        /*commandContext.getMessage()
            .getMessageChannel()
            .ofType(GuildMessageChannel.class)
            .flatMap(guildMessageChannel -> Mono.fromRunnable(() -> guildMessageChannel.getMessagesBefore(commandContext.getMessage().getId())
                .take(4)
                .filter(filterMessage -> filterMessage.getContent().contains("test"))
                .transform(guildMessageChannel::bulkDeleteMessages)
                .subscribe()
            )).subscribe();*/
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<String> getExamples() {
        return Concurrent.newUnmodifiableList(
            "CraftedFury",
            "CraftedFury Pineapple"
        );
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            new Parameter(
                "name",
                "Mojang Username or Unique ID",
                Parameter.Type.WORD,
                false,
                (argument, commandContext) -> StringUtil.isUUID(argument) || MOJANG_NAME.matcher(argument).matches()
            ),
            new Parameter(
                "profile",
                "SkyBlock Profile Name",
                Parameter.Type.WORD,
                false,
                (argument, commandContext) -> true//SimplifiedApi.getRepositoryOf(ProfileModel.class).findFirst(ProfileModel::getKey, argument.toUpperCase()).isPresent()
            )
        );
    }

}
