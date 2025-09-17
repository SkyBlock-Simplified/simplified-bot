package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

import java.awt.*;
import java.time.Instant;

public interface AppGuildEmbed extends Model {

    AppGuild getGuild();

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
