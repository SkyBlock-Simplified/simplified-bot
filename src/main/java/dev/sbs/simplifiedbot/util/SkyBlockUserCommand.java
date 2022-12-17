package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.util.exception.DiscordException;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class SkyBlockUserCommand extends Command {

    public static final Pattern MOJANG_NAME = Pattern.compile("[\\w]{3,16}");

    protected SkyBlockUserCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected final @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {
        return this.subprocess(commandContext, new SkyBlockUser(commandContext));
    }

    protected abstract @NotNull Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser);

    @Override
    public @NotNull ConcurrentUnmodifiableList<String> getExampleArguments() {
        return Concurrent.newUnmodifiableList(
            "CraftedFury",
            "CraftedFury Pineapple"
        );
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder("name", "Minecraft Username or UUID", Parameter.Type.WORD)
                .withValidator((argument, commandContext) -> StringUtil.isUUID(argument) || MOJANG_NAME.matcher(argument).matches())
                .build(),
            Parameter.builder("profile", "SkyBlock Profile Name", Parameter.Type.WORD)
                .withValidator((argument, commandContext) -> SimplifiedApi.getRepositoryOf(ProfileModel.class).findFirst(ProfileModel::getKey, argument.toUpperCase()).isPresent())
                .withChoices(
                    SimplifiedApi.getRepositoryOf(ProfileModel.class)
                        .findAll()
                        .stream()
                        .map(profileModel -> Pair.of(profileModel.getName(), profileModel.getKey()))
                        .collect(Concurrent.toMap())
                )
                .build()
        );
    }

    protected static Embed.EmbedBuilder getEmbedBuilder(MojangProfileResponse mojangProfile, SkyBlockIsland skyBlockIsland, String identifier, String title) {
        return Embed.builder()
            .withAuthor(title)
            .withColor(Color.DARK_GRAY)
            .withTitle(
                "{0} :: {1} ({2}{3})",
                WordUtil.capitalizeFully(identifier.replace("_", " ")),
                mojangProfile.getUsername(),
                skyBlockIsland.getProfileName()
                    .map(ProfileModel::getEmoji)
                    .flatMap(Emoji::of)
                    .map(Emoji::asSpacedFormat)
                    .orElse(""),
                skyBlockIsland.getProfileName().map(ProfileModel::getName).orElse("")
            )
            .withTimestamp(Instant.now())
            .withThumbnailUrl(
                "https://api.sbs.dev/mojang/avatar/{0}",
                mojangProfile.getUsername()
            );
    }

    protected static <T extends SkyBlockIsland.Experience> Embed getSkillEmbed(
        MojangProfileResponse mojangProfile,
        SkyBlockIsland skyBlockIsland,
        String value,
        ConcurrentList<T> experienceObjects,
        double average,
        double experience,
        double totalProgress,
        Function<T, String> nameFunction
    ) {
        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");

        return getEmbedBuilder(mojangProfile, skyBlockIsland, value, "Player Information")
            .withField(
                "Details",
                FormatUtil.format(
                    """
                    {0}Average Level: {2,number,#.##}
                    {0}Total Experience: {3}
                    {1}Total Progress: {4,number,#.##}%
                    """,
                    emojiReplyStem,
                    emojiReplyEnd,
                    average,
                    experience,
                    totalProgress
                )
            )
            .withFields(
                Field.builder()
                    .withName(WordUtil.capitalizeFully(value.replace("_", " ")))
                    .withValue(
                        StringUtil.join(
                            experienceObjects.stream()
                                .map(nameFunction)
                                .collect(Concurrent.toList()),
                            "\n"
                        )
                    )
                    .isInline()
                    .build(),
                Field.builder()
                    .withName("Level (Progress)")
                    .withValue(
                        StringUtil.join(
                            experienceObjects.stream()
                                .map(expObject -> FormatUtil.format(
                                    "{0} ({1,number,#.##}%)",
                                    expObject.getLevel(),
                                    expObject.getTotalProgressPercentage()
                                ))
                                .collect(Concurrent.toList()),
                            "\n"
                        )
                    )
                    .isInline()
                    .build(),
                Field.builder()
                    .withName("Experience")
                    .withValue(
                        StringUtil.join(
                            experienceObjects.stream()
                                .map(expObject -> FormatUtil.format(
                                    "{0,number}",
                                    expObject.getExperience()
                                ))
                                .collect(Concurrent.toList()),
                            "\n"
                        )
                    )
                    .isInline()
                    .build()
            )
            .build();
    }

}
