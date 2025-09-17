package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface SbsLegacyDonor extends Model {

    Long getDiscordId();

    Double getAmount();

}
