package dev.sbs.simplifiedbot.data.skyblock.sack_items;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;
import dev.sbs.simplifiedbot.data.skyblock.sacks.SackModel;

public interface SackItemModel extends Model {

    SackModel getSack();

    ItemModel getItem();

}
