package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.SkyBlockIsland;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.util.Experience;
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
import java.util.Optional;
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
                .isRequired()
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

    protected static Embed.EmbedBuilder getEmbedBuilder(MojangProfileResponse mojangProfile, SkyBlockIsland skyBlockIsland, String identifier) {
        return Embed.builder()
            .withAuthor(WordUtil.capitalizeFully(identifier.replace("_", " ")))
            .withColor(Color.DARK_GRAY)
            .withTitle(
                "{0} :: {1}{2}",
                mojangProfile.getUsername(),
                skyBlockIsland.getProfileModel()
                    .map(ProfileModel::getEmoji)
                    .flatMap(Emoji::of)
                    .map(Emoji::asSpacedFormat)
                    .orElse(""),
                skyBlockIsland.getProfileModel()
                    .map(ProfileModel::getName)
                    .orElse("")
            )
            .withTimestamp(Instant.now())
            .withThumbnailUrl(
                "https://crafatar.com/avatars/{0}?overlay",
                mojangProfile.getUniqueId()
            );
    }

    protected static <T extends Experience> Embed getSkillEmbed(
        MojangProfileResponse mojangProfile,
        SkyBlockIsland skyBlockIsland,
        String value,
        ConcurrentList<T> experienceObjects,
        double average,
        double experience,
        double totalProgress,
        Function<T, String> nameFunction,
        Function<T, Optional<Emoji>> emojiFunction,
        boolean details
    ) {
        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyLine = getEmoji("REPLY_LINE").map(Emoji::asPreSpacedFormat).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        Embed.EmbedBuilder startBuilder;

        if (details) {
            startBuilder = getEmbedBuilder(mojangProfile, skyBlockIsland, value)
                .withField(
                    "Details",
                    FormatUtil.format(
                        """
                            {0}Average Level: **{2,number,#.##}**
                            {0}Total Experience: **{3,number,#,###}**
                            {1}Total Progress: **{4,number,#.##}%**
                            """,
                        emojiReplyStem,
                        emojiReplyEnd,
                        average,
                        experience,
                        totalProgress
                    )
                );
        } else
            startBuilder = getEmbedBuilder(mojangProfile, skyBlockIsland, value);

        return startBuilder.withFields(
                experienceObjects.stream()
                    .map(experienceObject -> Field.builder()
                        .withName(WordUtil.capitalizeFully(nameFunction.apply(experienceObject).replace("_", " ")))
                        .withValue(FormatUtil.format(
                            """
                            {0}Level: **{3,number,#.##}**
                            {0}Experience:
                            {1}**{4,number,#,###}**
                            {2}Progress: **{5,number,#.##}%**
                            """,
                            emojiReplyStem,
                            emojiReplyLine,
                            emojiReplyEnd,
                            experienceObject.getLevel(),
                            (long) experienceObject.getExperience(),
                            experienceObject.getTotalProgressPercentage()
                        ))
                        .withEmoji(emojiFunction.apply(experienceObject))
                        .isInline()
                        .build()
                    )
                    .collect(Concurrent.toList())
            )
            .withEmptyField(true)
            .build();
    }

}
