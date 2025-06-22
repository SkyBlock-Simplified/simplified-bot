package dev.sbs.simplifiedbot.data.skyblock.hotm_perk_stats;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.hotm_perks.HotmPerkModel;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatModel;

public interface HotmPerkStatModel extends Model {

    HotmPerkModel getPerk();

    StatModel getStat();

}
