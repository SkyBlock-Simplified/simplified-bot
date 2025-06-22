package dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeon_fairy_souls;

import dev.sbs.api.data.model.Model;

public interface DungeonFairySoulModel extends Model {

    String getRoom();

    String getDescription();

    String getWhere();

    boolean isWalkable();

}
