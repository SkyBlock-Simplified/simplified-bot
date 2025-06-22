package dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_application_requirements;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.application_requirements.ApplicationRequirementModel;
import dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_applications.GuildApplicationModel;
import dev.sbs.simplifiedbot.data.discord.setting_types.SettingTypeModel;

public interface GuildApplicationRequirementModel extends Model {

    GuildApplicationModel getApplication();

    ApplicationRequirementModel getRequirement();

    SettingTypeModel getType();

    String getValue();

    String getDescription();

}
