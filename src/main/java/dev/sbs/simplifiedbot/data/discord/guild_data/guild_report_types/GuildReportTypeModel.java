package dev.sbs.simplifiedbot.data.discord.guild_data.guild_report_types;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildModel;

public interface GuildReportTypeModel extends Model {

    GuildModel getGuild();

    String getKey();

    String getName();

    String getDescription();

}
