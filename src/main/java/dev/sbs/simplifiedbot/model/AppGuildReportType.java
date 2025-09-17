package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface AppGuildReportType extends Model {

    AppGuild getGuild();

    String getKey();

    String getName();

    String getDescription();

}
