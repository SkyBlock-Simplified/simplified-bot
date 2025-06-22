package dev.sbs.simplifiedbot.optimizer.modules.common;


import dev.sbs.simplifiedbot.data.skyblock.item_types.ItemTypeModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RarityModel;
import dev.sbs.simplifiedbot.profile_stats.data.ObjectData;

import java.util.List;
import java.util.UUID;

public interface ItemEntity {

    List<ReforgeFact> getAvailableReforges();

    default RarityModel getRarity() {
        return this.getObjectData().getRarity();
    }

    ReforgeFact getReforgeFact();

    ObjectData<?> getObjectData();

    ItemTypeModel getType();

    UUID getUniqueId();

}
