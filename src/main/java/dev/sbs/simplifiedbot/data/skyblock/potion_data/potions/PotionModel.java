package dev.sbs.simplifiedbot.data.skyblock.potion_data.potions;

import dev.sbs.api.data.model.Model;

public interface PotionModel extends Model {

    String getKey();

    String getName();

    String getDescription();

    boolean isBuff();

}
