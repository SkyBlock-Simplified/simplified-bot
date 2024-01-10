package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.SkyBlockIsland;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.util.Experience;
import dev.sbs.api.client.impl.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.discord.users.UserModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.api.util.mutable.pair.Pair;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.structure.Author;
import dev.sbs.discordapi.response.embed.structure.Field;
import dev.sbs.discordapi.response.embed.structure.Footer;
import dev.sbs.discordapi.util.exception.DiscordException;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class SkyBlockUserCommand extends SqlSlashCommand {

    public static final Pattern MOJANG_NAME = Pattern.compile("[\\w]{3,16}");

    protected SkyBlockUserCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected final @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return this.subprocess(commandContext, new SkyBlockUser(commandContext));
    }

    protected abstract @NotNull Mono<Void> subprocess(@NotNull SlashCommandContext commandContext, @NotNull SkyBlockUser skyBlockUser);

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
            Parameter.builder()
                .withName("name")
                .withDescription("Minecraft Username or UUID")
                .withType(Parameter.Type.WORD)
                .withValidator((argument, commandContext) -> StringUtil.isUUID(argument) || MOJANG_NAME.matcher(argument).matches())
                .isRequired()
                .build(),
            Parameter.builder()
                .withName("profile")
                .withDescription("SkyBlock Profile Name")
                .withType(Parameter.Type.WORD)
                .withValidator((argument, commandContext) -> SimplifiedApi.getRepositoryOf(ProfileModel.class).findFirst(ProfileModel::getKey, argument.toUpperCase()).isPresent())
                .withChoices(
                    SimplifiedApi.getRepositoryOf(ProfileModel.class)
                        .stream()
                        .map(profileModel -> Pair.of(profileModel.getName(), profileModel.getKey()))
                        .collect(Concurrent.toWeakLinkedMap())
                )
                .build()
        );
    }

    protected static Embed.Builder getEmbedBuilder(MojangProfileResponse mojangProfile, SkyBlockIsland skyBlockIsland, String identifier) {
        return Embed.builder()
            .withAuthor(
                Author.builder()
                    .withName(mojangProfile.getUsername())
                    .build()
            )
            .withColor(Color.DARK_GRAY)
            .withTitle(StringUtil.capitalizeFully(identifier.replace("_", " ")))
            .withFooter(
                Footer.builder()
                    .withText(skyBlockIsland.getProfileName().orElse(""))
                    .withIconUrl(
                        skyBlockIsland.getProfileName()
                            .flatMap(profileName -> SimplifiedApi.getRepositoryOf(ProfileModel.class)
                                .findFirst(ProfileModel::getKey, profileName)
                            )
                            .map(ProfileModel::getEmoji)
                            .flatMap(Emoji::of)
                            .map(Emoji::getUrl)
                    )
                    .withTimestamp(Instant.now())
                    .build()
            )
            .withThumbnailUrl(
                "https://crafatar.com/avatars/%s?overlay",
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
        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> String.format("%s ", emoji.asFormat())).orElse("");
        String emojiReplyLine = getEmoji("REPLY_LINE").map(Emoji::asPreSpacedFormat).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> String.format("%s ", emoji.asFormat())).orElse("");
        Embed.Builder startBuilder;

        if (details) {
            startBuilder = getEmbedBuilder(mojangProfile, skyBlockIsland, value)
                .withField(
                    "Details",
                    String.format(
                        """
                            %1$sAverage Level: **%3$.2f**
                            %1$sTotal Experience: **%4$,f**
                            %2$sTotal Progress: **%5$.2f%%**""",
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
                        .withName(StringUtil.capitalizeFully(nameFunction.apply(experienceObject).replace("_", " ")))
                        .withValue(String.format(
                            """
                                %1$sLevel: **%4$s**
                                %1$sExperience:
                                %2$s**%5$.2f**
                                %3$sProgress: **%6$.2f%%**""",
                            emojiReplyStem,
                            emojiReplyLine,
                            emojiReplyEnd,
                            experienceObject.getLevel(),
                            experienceObject.getExperience(),
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

    public final boolean isUserVerified(@NotNull UUID uniqueId) {
        return SimplifiedApi.getRepositoryOf(UserModel.class).matchFirst(userModel -> userModel.getMojangUniqueIds().contains(uniqueId)).isPresent();
    }

}
