package dev.sbs.simplifiedbot.data.skyblock.bonus_data.bonus_pet_ability_stats;

import dev.sbs.api.data.model.BuffEffectsModel;
import dev.sbs.simplifiedbot.data.discord.optimizer_mob_types.OptimizerMobTypeModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;
import dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_abilities.PetAbilityModel;

public interface BonusPetAbilityStatModel extends BuffEffectsModel<Object, Double> {

    PetAbilityModel getPetAbility();

    boolean isPercentage();

    ItemModel getRequiredItem();

    OptimizerMobTypeModel getRequiredMobType();

    default boolean hasRequiredItem() {
        return this.getRequiredItem() != null;
    }

    default boolean hasRequiredMobType() {
        return this.getRequiredMobType() != null;
    }

    default boolean notPercentage() {
        return !this.isPercentage();
    }

    default boolean noRequiredItem() {
        return !this.hasRequiredItem();
    }

    default boolean noRequiredMobType() {
        return !this.hasRequiredMobType();
    }

}
