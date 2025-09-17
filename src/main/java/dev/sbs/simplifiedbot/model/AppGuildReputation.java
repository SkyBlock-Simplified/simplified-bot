package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface AppGuildReputation extends Model {

    AppGuildReputationType getType();

    Long getReceiverDiscordId();

    Long getSubmitterDiscordId();

    Long getAssigneeDiscordId();

    String getReason();

}
