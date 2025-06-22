package dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforges;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;
import dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforge_conditions.ReforgeConditionModel;

import java.util.List;

public interface ReforgeModel extends Model {

    String getKey();

    String getName();

    ItemModel getItem();

    List<String> getItemTypes();

    default boolean isBlacksmith() {
        return this.getItem() == null;
    }

    default boolean isNotBlacksmith() {
        return !this.isBlacksmith();
    }

    default boolean isStone() {
        return this.getItem() != null;
    }

    default boolean isNotStone() {
        return !this.isStone();
    }

    default List<ReforgeConditionModel> getConditions() {
        return SimplifiedApi.getRepositoryOf(ReforgeConditionModel.class)
            .findAll(ReforgeConditionModel::getReforge, this)
            .collect(Concurrent.toUnmodifiableList());
    }

}
