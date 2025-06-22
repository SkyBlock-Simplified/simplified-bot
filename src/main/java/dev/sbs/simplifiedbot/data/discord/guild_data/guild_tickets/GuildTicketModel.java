package dev.sbs.simplifiedbot.data.discord.guild_data.guild_tickets;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.guild_embeds.GuildEmbedModel;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildModel;

public interface GuildTicketModel extends Model {

    GuildModel getGuild();

    String getKey();

    String getName();

    GuildEmbedModel getEmbed();

    boolean isEnabled();

    String getNotes();

}
