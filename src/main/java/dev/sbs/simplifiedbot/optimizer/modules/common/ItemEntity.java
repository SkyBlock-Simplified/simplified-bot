package dev.sbs.simplifiedbot.optimizer.modules.common;


import dev.sbs.minecraftapi.model.ItemCategory;
import dev.sbs.minecraftapi.skyblock.common.Rarity;
import dev.sbs.simplifiedbot.profile_stats.data.ObjectData;

import java.util.List;
import java.util.UUID;

public interface ItemEntity {

    List<ReforgeFact> getAvailableReforges();

    default Rarity getRarity() {
        return this.getObjectData().getRarity();
    }

    ReforgeFact getReforgeFact();

    ObjectData<?> getObjectData();

    ItemCategory getType();

    UUID getUniqueId();

}
