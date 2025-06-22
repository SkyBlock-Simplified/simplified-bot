package dev.sbs.simplifiedbot.data.skyblock.bonus_data.bonus_reforge_stats;

import dev.sbs.api.data.model.BuffEffectsModel;
import dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforges.ReforgeModel;

public interface BonusReforgeStatModel extends BuffEffectsModel<Object, Double> {

    ReforgeModel getReforge();

}
