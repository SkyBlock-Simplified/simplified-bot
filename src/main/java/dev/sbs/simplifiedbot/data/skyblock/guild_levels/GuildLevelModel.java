package dev.sbs.simplifiedbot.data.skyblock.guild_levels;

import dev.sbs.api.data.model.Model;

public interface GuildLevelModel extends Model {

    Integer getLevel();

    Double getTotalExpRequired();

}
