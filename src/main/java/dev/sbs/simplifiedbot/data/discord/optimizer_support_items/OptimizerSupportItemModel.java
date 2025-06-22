package dev.sbs.simplifiedbot.data.discord.optimizer_support_items;

import dev.sbs.api.data.model.EffectsModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;

public interface OptimizerSupportItemModel extends EffectsModel<Double> {

    ItemModel getItem();

}
