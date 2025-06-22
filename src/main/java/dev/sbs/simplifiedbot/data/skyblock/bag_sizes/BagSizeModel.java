package dev.sbs.simplifiedbot.data.skyblock.bag_sizes;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.bags.BagModel;

public interface BagSizeModel extends Model {

    BagModel getBag();

    Integer getCollectionTier();

    Integer getSlotCount();

}
