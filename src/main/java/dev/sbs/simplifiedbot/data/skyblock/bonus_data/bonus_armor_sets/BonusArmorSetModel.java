package dev.sbs.simplifiedbot.data.skyblock.bonus_data.bonus_armor_sets;

import dev.sbs.api.data.model.BuffEffectsModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;

public interface BonusArmorSetModel extends BuffEffectsModel<Object, Double> {

    String getKey();

    String getName();

    ItemModel getHelmetItem();

    ItemModel getChestplateItem();

    ItemModel getLeggingsItem();

    ItemModel getBootsItem();

}
