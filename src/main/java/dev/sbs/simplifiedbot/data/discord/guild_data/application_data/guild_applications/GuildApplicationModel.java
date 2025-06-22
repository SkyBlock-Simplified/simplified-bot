package dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_applications;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_application_types.GuildApplicationTypeModel;
import dev.sbs.simplifiedbot.data.discord.guild_data.guild_embeds.GuildEmbedModel;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildModel;

import java.time.Instant;

public interface GuildApplicationModel extends Model {

    String getKey();

    String getName();

    GuildModel getGuild();

    GuildApplicationTypeModel getType();

    GuildEmbedModel getEmbed();

    boolean isEnabled();

    String getNotes();

    Instant getLiveAt();

    Instant getCloseAt();

}
