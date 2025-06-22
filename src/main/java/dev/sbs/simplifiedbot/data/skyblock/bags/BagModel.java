package dev.sbs.simplifiedbot.data.skyblock.bags;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.collection_data.collection_items.CollectionItemModel;

public interface BagModel extends Model {

    String getKey();

    String getName();

    CollectionItemModel getCollectionItem();

}
