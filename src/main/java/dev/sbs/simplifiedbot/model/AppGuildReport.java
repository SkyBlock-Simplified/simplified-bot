package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

import java.util.List;

public interface AppGuildReport extends Model {

    AppGuildReportType getType();

    Long getReportedDiscordId();

    String getReportedMojangUniqueId();

    Long getSubmitterDiscordId();

    Long getAssigneeDiscordId();

    String getReason();

    String getProof();

    List<String> getMediaLinks();

    String getNotes();

}
