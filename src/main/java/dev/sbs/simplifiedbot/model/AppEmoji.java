package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;
import dev.sbs.discordapi.context.reaction.ReactionContext;
import dev.sbs.discordapi.response.Emoji;
import discord4j.common.util.Snowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface AppEmoji extends Model {

    Long getEmojiId();

    AppGuild getGuild();

    String getKey();

    String getName();

    boolean isAnimated();

    default @NotNull String getUrl() {
        return Emoji.getUrl(this.getEmojiId(), this.isAnimated());
    }

    default @NotNull Emoji getDiscordEmoji() {
        return this.getDiscordEmoji(null);
    }

    default @NotNull Emoji getDiscordEmoji(@Nullable Function<ReactionContext, Mono<Void>> interaction) {
        return Emoji.of(Snowflake.of(this.getEmojiId()), this.getKey(), this.isAnimated(), interaction);
    }

}
