package dev.sbs.simplifiedbot.data.skyblock.minion_data.minion_items;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.collection_data.collection_items.CollectionItemModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;
import dev.sbs.simplifiedbot.data.skyblock.minion_data.minions.MinionModel;

public interface MinionItemModel extends Model {

    MinionModel getMinion();

    CollectionItemModel getCollectionItem();

    ItemModel getItem();

    Double getAverageYield();

}
