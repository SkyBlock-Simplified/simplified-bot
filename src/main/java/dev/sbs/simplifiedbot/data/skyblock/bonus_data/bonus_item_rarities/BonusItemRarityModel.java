package dev.sbs.simplifiedbot.data.skyblock.bonus_data.bonus_item_rarities;

import dev.sbs.api.data.model.BuffEffectsModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;

public interface BonusItemRarityModel extends BuffEffectsModel<Object, Double> {

    ItemModel getItem();

}
