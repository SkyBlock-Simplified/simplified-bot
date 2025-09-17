package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.DiscordCommand;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.exception.DiscordException;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.structure.Author;
import dev.sbs.discordapi.response.embed.structure.Field;
import dev.sbs.discordapi.response.embed.structure.Footer;
import dev.sbs.minecraftapi.client.mojang.profile.MojangProfile;
import dev.sbs.minecraftapi.skyblock.island.Profile;
import dev.sbs.minecraftapi.skyblock.island.SkyBlockIsland;
import dev.sbs.minecraftapi.skyblock.type.Experience;
import dev.sbs.simplifiedbot.model.AppUser;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class SkyBlockUserCommand extends DiscordCommand<SlashCommandContext> {

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
                .withValidator((argument, commandContext) -> Profile.of(argument.toUpperCase()).isPresent())
                .withChoices(Profile.CHOICES)
                .build()
        );
    }

    protected static Embed.Builder getEmbedBuilder(MojangProfile mojangProfile, SkyBlockIsland skyBlockIsland, String identifier) {
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
                    .withText(
                        "%s %s",
                        skyBlockIsland.getProfile().getSymbol(),
                        skyBlockIsland.getProfile().getName()
                    )
                    .withTimestamp(Instant.now())
                    .build()
            )
            .withThumbnailUrl(
                "https://crafatar.com/avatars/%s?overlay",
                mojangProfile.getUniqueId()
            );
    }

    protected <T extends Experience> Embed getSkillEmbed(
        MojangProfile mojangProfile,
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
        String emojiReplyStem = this.getEmoji("REPLY_STEM").map(emoji -> String.format("%s ", emoji.asFormat())).orElse("");
        String emojiReplyLine = this.getEmoji("REPLY_LINE").map(Emoji::asPreSpacedFormat).orElse("");
        String emojiReplyEnd = this.getEmoji("REPLY_END").map(emoji -> String.format("%s ", emoji.asFormat())).orElse("");
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
        return SimplifiedApi.getRepositoryOf(AppUser.class).matchFirst(userModel -> userModel.getMojangUniqueIds().contains(uniqueId)).isPresent();
    }

}
