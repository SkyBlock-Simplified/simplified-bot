package dev.sbs.simplifiedbot.data.skyblock.accessory_data.accessory_powers;

import dev.sbs.api.data.model.EffectsModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;

import java.util.Map;

public interface AccessoryPowerModel extends EffectsModel<Double> {

    String getKey();

    String getName();

    ItemModel getItem();

    Integer getMinimumCombatLevel();

    default boolean isStone() {
        return this.getItem() != null;
    }

    default boolean isNotStone() {
        return !this.isStone();
    }

    Map<String, Double> getUniqueEffects();

    default Double getBuffEffect(String key) {
        return this.getBuffEffect(key, null);
    }

    default Double getBuffEffect(String key, Double defaultValue) {
        return this.getUniqueEffects().getOrDefault(key, defaultValue);
    }

}
