package dev.sbs.simplifiedbot.data.discord.guild_data.guild_embeds;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildModel;

import java.awt.*;
import java.time.Instant;

public interface GuildEmbedModel extends Model {

    GuildModel getGuild();

    String getKey();

    String getTitle();

    Color getColor();

    String getUrl();

    String getDescription();

    String getAuthorName();

    String getAuthorUrl();

    String getAuthorIconUrl();

    String getImageUrl();

    String getThumbnailUrl();

    String getVideoUrl();

    Instant getTimestamp();

    String getFooterText();

    String getFooterIconUrl();

    String getNotes();

    Long getSubmitterDiscordId();

    Long getEditorDiscordId();

}
