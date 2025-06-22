package dev.sbs.simplifiedbot.data.skyblock.gemstone_data.gemstones;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatModel;

public interface GemstoneModel extends Model {

    String getKey();

    String getName();

    StatModel getStat();

}
