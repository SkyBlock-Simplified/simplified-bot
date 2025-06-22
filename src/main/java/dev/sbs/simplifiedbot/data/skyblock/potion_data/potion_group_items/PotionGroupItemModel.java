package dev.sbs.simplifiedbot.data.skyblock.potion_data.potion_group_items;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.potion_data.potion_groups.PotionGroupModel;
import dev.sbs.simplifiedbot.data.skyblock.potion_data.potion_tiers.PotionTierModel;

public interface PotionGroupItemModel extends Model {

    PotionGroupModel getPotionGroup();

    PotionTierModel getPotionTier();

}
