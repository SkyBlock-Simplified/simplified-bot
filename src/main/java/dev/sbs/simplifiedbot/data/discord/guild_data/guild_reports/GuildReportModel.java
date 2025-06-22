package dev.sbs.simplifiedbot.data.discord.guild_data.guild_reports;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.guild_report_types.GuildReportTypeModel;

import java.util.List;

public interface GuildReportModel extends Model {

    GuildReportTypeModel getType();

    Long getReportedDiscordId();

    String getReportedMojangUniqueId();

    Long getSubmitterDiscordId();

    Long getAssigneeDiscordId();

    String getReason();

    String getProof();

    List<String> getMediaLinks();

    String getNotes();

}
