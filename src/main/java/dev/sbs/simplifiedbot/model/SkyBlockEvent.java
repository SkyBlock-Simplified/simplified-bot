package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface SkyBlockEvent extends Model {

    String getKey();

    String getName();

    AppEmoji getBotEmoji();

    String getDescription();

    boolean isEnabled();

    String getStatus();

    String getIntervalExpression();

    String getThirdPartyJsonUrl();

}
