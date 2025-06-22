package dev.sbs.simplifiedbot.data.skyblock.potion_data.potion_brews;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.npcs.NpcModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RarityModel;

public interface PotionBrewModel extends Model {

    String getKey();

    String getName();

    RarityModel getRarity();

    String getDescription();

    NpcModel getNpc();

    Integer getCoinCost();

    Integer getAmplified();

}
