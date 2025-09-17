package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface AppSetting extends Model {

    String getKey();

    String getName();

    AppSettingType getType();

    String getValue();

}
