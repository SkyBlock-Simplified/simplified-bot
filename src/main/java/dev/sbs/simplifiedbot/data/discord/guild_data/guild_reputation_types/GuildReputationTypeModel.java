package dev.sbs.simplifiedbot.data.discord.guild_data.guild_reputation_types;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildModel;

public interface GuildReputationTypeModel extends Model {

    GuildModel getGuild();

    String getKey();

    String getName();

    String getDescription();

    boolean isEnabled();

}
