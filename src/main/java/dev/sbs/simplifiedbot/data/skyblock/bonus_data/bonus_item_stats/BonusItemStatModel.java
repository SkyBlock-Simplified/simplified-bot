package dev.sbs.simplifiedbot.data.skyblock.bonus_data.bonus_item_stats;

import dev.sbs.api.data.model.BuffEffectsModel;
import dev.sbs.simplifiedbot.data.discord.optimizer_mob_types.OptimizerMobTypeModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;

public interface BonusItemStatModel extends BuffEffectsModel<Object, Double> {

    ItemModel getItem();

    boolean isForStats();

    boolean isForReforges();

    boolean isForGems();

    OptimizerMobTypeModel getRequiredMobType();

    default boolean hasRequiredMobType() {
        return this.getRequiredMobType() != null;
    }

    default boolean noRequiredMobType() {
        return !this.hasRequiredMobType();
    }

}
