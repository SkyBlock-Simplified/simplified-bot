package dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_application_types;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildModel;

public interface GuildApplicationTypeModel extends Model {

    GuildModel getGuild();

    String getKey();

    String getName();

    String getDescription();

}
