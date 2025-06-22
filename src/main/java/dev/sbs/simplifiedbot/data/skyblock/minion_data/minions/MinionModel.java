package dev.sbs.simplifiedbot.data.skyblock.minion_data.minions;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.collection_data.collections.CollectionModel;

public interface MinionModel extends Model {

    String getKey();

    CollectionModel getCollection();

    String getName();

}
