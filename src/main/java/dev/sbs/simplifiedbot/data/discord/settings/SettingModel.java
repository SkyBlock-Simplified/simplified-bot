package dev.sbs.simplifiedbot.data.discord.settings;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.setting_types.SettingTypeModel;

public interface SettingModel extends Model {

    String getKey();

    String getName();

    SettingTypeModel getType();

    String getValue();

}
