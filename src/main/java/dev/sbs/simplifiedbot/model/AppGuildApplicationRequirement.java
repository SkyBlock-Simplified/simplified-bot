package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface AppGuildApplicationRequirement extends Model {

    AppGuildApplication getApplication();

    AppApplicationRequirement getRequirement();

    AppSettingType getType();

    String getValue();

    String getDescription();

}
