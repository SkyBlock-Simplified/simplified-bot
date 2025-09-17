package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

import java.time.Instant;

public interface AppGuildApplication extends Model {

    String getKey();

    String getName();

    AppGuild getGuild();

    AppGuildApplicationType getType();

    AppGuildEmbed getEmbed();

    boolean isEnabled();

    String getNotes();

    Instant getLiveAt();

    Instant getCloseAt();

}
