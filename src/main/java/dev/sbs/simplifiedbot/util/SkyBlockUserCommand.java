package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.mojang.response.MojangProfileResponse;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.util.exception.DiscordException;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;
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
            new Parameter(
                "name",
                "Minecraft Username or UUID",
                Parameter.Type.WORD,
                false,
                (argument, commandContext) -> StringUtil.isUUID(argument) || MOJANG_NAME.matcher(argument).matches()
            ),
            new Parameter(
                "profile",
                "SkyBlock Profile Name",
                Parameter.Type.WORD,
                false,
                (argument, commandContext) -> SimplifiedApi.getRepositoryOf(ProfileModel.class).findFirst(ProfileModel::getKey, argument.toUpperCase()).isPresent()
            )
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

}
