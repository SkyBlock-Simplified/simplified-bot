package dev.sbs.simplifiedbot.data.skyblock.minion_data.minion_tier_upgrades;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;
import dev.sbs.simplifiedbot.data.skyblock.minion_data.minion_tiers.MinionTierModel;

public interface MinionTierUpgradeModel extends Model {

    MinionTierModel getMinionTier();

    Double getCoinCost();

    ItemModel getItemCost();

    Integer getItemQuantity();

}
