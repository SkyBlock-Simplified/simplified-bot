package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

import java.util.List;

public interface AppGuildSkyBlockEvent extends Model {

    AppGuild getGuild();

    SkyBlockEvent getEvent();

    boolean isEnabled();

    List<Long> getMentionRoles();

    String getWebhookUrl();

}
