package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface AppGuildApplicationEntry extends Model {

    AppGuildApplication getApplication();

    Long getSubmitterDiscordId();

}
