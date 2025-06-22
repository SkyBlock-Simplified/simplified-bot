package dev.sbs.simplifiedbot.data.discord.emojis;

import dev.sbs.api.data.model.Model;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildModel;

public interface EmojiModel extends Model {

    Long getEmojiId();

    GuildModel getGuild();

    String getKey();

    String getName();

    boolean isAnimated();

    default String getUrl() {
        return Emoji.getUrl(this.getEmojiId(), this.isAnimated());
    }

}
