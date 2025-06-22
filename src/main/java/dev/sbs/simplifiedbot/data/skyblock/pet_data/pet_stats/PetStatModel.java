package dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_stats;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.pet_data.pets.PetModel;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatModel;

import java.util.List;

public interface PetStatModel extends Model {

    PetModel getPet();

    StatModel getStat();

    Integer getOrdinal();

    List<Integer> getRarities();

    Double getBaseValue();

    Double getLevelBonus();

}
