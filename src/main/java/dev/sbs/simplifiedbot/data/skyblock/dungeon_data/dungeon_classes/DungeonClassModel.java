package dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeon_classes;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiModel;

public interface DungeonClassModel extends Model {

    String getKey();

    String getName();

    EmojiModel getEmoji();

    Double getWeightMultiplier();

}
