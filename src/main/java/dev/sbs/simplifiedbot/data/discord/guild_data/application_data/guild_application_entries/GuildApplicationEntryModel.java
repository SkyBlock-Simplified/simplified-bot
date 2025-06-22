package dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_application_entries;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_applications.GuildApplicationModel;

public interface GuildApplicationEntryModel extends Model {

    GuildApplicationModel getApplication();

    Long getSubmitterDiscordId();

}
