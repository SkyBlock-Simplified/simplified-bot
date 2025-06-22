package dev.sbs.simplifiedbot.data.discord.setting_types;

import dev.sbs.api.data.model.Model;

public interface SettingTypeModel extends Model {

    String getKey();

    String getName();

}
