package dev.sbs.simplifiedbot.data.discord.guild_data.guild_reputation;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.guild_reputation_types.GuildReputationTypeModel;

public interface GuildReputationModel extends Model {

    GuildReputationTypeModel getType();

    Long getReceiverDiscordId();

    Long getSubmitterDiscordId();

    Long getAssigneeDiscordId();

    String getReason();

}
