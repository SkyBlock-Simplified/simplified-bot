package dev.sbs.simplifiedbot.data.discord.guild_data.guilds;

import dev.sbs.api.data.model.Model;

public interface GuildModel extends Model {

    Long getGuildId();

    String getName();

    boolean isReportsPublic();

    boolean isEmojiServer();

}
