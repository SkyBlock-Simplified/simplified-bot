package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface AppGuildTicket extends Model {

    AppGuild getGuild();

    String getKey();

    String getName();

    AppGuildEmbed getEmbed();

    boolean isEnabled();

    String getNotes();

}
