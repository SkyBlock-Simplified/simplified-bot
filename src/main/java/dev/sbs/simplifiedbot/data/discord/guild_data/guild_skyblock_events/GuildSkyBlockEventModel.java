package dev.sbs.simplifiedbot.data.discord.guild_data.guild_skyblock_events;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildModel;
import dev.sbs.simplifiedbot.data.discord.skyblock_events.SkyBlockEventModel;

import java.util.List;

public interface GuildSkyBlockEventModel extends Model {

    GuildModel getGuild();

    SkyBlockEventModel getEvent();

    boolean isEnabled();

    List<Long> getMentionRoles();

    String getWebhookUrl();

}
