package dev.sbs.simplifiedbot.data.skyblock.essence_perks;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatModel;

public interface EssencePerkModel extends Model {

    String getKey();

    StatModel getStat();

    Integer getLevelBonus();

    boolean isPermanent();

}
