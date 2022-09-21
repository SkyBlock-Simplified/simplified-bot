package dev.sbs.simplifiedbot.optimizer.modules.common;

import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ObjectData;
import dev.sbs.api.data.model.skyblock.item_types.ItemTypeModel;
import dev.sbs.api.data.model.skyblock.rarities.RarityModel;

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
