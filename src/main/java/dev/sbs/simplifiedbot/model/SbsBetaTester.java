package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface SbsBetaTester extends Model {

    Long getDiscordId();

    boolean isEarly();

}
