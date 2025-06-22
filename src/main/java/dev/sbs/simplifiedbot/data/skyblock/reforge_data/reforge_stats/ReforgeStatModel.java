package dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforge_stats;

import dev.sbs.api.data.model.EffectsModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RarityModel;
import dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforges.ReforgeModel;

public interface ReforgeStatModel extends EffectsModel<Double> {

    ReforgeModel getReforge();

    RarityModel getRarity();

}
