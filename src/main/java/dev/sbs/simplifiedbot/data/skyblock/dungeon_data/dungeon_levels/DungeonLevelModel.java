package dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeon_levels;

import dev.sbs.api.data.model.BuffEffectsModel;

public interface DungeonLevelModel extends BuffEffectsModel<Double, Double> {

    Integer getLevel();

    Double getTotalExpRequired();

    Integer getStatMultiplier();

}
