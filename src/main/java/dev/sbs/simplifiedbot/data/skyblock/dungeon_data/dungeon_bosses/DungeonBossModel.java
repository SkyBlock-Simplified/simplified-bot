package dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeon_bosses;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiModel;

public interface DungeonBossModel extends Model {

    String getKey();

    String getName();

    String getDescription();

    EmojiModel getEmoji();

}
