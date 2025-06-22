package dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeons;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiModel;

public interface DungeonModel extends Model {

    String getKey();

    String getName();

    EmojiModel getEmoji();

    Double getWeightMultiplier();

    boolean isMasterModeEnabled();

}
