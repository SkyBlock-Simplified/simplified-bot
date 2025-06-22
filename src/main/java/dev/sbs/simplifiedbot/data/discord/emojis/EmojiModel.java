package dev.sbs.simplifiedbot.data.discord.emojis;

import dev.sbs.api.data.model.Model;
import dev.sbs.discordapi.context.reaction.ReactionContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildModel;
import discord4j.common.util.Snowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface EmojiModel extends Model {

    Long getEmojiId();

    GuildModel getGuild();

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
