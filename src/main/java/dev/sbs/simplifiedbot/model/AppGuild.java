package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface AppGuild extends Model {

    Long getGuildId();

    String getName();

    boolean isReportsPublic();

    boolean isEmojiServer();

}
