package dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforge_conditions;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;
import dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforges.ReforgeModel;

public interface ReforgeConditionModel extends Model {

    ReforgeModel getReforge();

    ItemModel getItem();

}
