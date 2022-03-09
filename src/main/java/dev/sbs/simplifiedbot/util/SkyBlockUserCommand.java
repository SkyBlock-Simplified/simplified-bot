package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

public abstract class SkyBlockUserCommand extends Command {

    public static final Pattern MOJANG_NAME = Pattern.compile("[\\w]{3,16}");

    protected SkyBlockUserCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected final Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {
        return Mono.just(commandContext).flatMap(context -> this.subprocess(context, new SkyBlockUser(context)));
    }

    protected abstract Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser);

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

}
