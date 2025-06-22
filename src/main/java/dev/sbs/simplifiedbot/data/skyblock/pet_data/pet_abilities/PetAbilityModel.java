package dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_abilities;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.pet_data.pets.PetModel;

public interface PetAbilityModel extends Model {

    String getKey();

    String getName();

    PetModel getPet();

    Integer getOrdinal();

    String getDescription();

}
