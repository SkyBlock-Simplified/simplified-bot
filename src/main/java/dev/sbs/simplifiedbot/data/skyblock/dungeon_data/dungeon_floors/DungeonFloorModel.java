package dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeon_floors;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeon_bosses.DungeonBossModel;
import dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeon_floor_sizes.DungeonFloorSizeModel;
import dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeons.DungeonModel;

public interface DungeonFloorModel extends Model {

    DungeonModel getDungeon();

    Integer getFloor();

    DungeonFloorSizeModel getFloorSize();

    DungeonBossModel getFloorBoss();

}
