package dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_ability_stats;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_abilities.PetAbilityModel;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatModel;

import java.util.List;

public interface PetAbilityStatModel extends Model {

    PetAbilityModel getAbility();

    StatModel getStat();

    List<Integer> getRarities();

    Double getBaseValue();

    Double getLevelBonus();

    boolean isRoundingNeeded();

}
