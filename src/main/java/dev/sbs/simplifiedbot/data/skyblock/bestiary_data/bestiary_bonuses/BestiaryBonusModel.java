package dev.sbs.simplifiedbot.data.skyblock.bestiary_data.bestiary_bonuses;

import dev.sbs.api.data.model.EffectsModel;

public interface BestiaryBonusModel extends EffectsModel<Double> {

    Integer getLevel();

}
