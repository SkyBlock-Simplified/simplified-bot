package dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_levels;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RarityModel;

public interface PetLevelModel extends Model {

    RarityModel getRarity();

    default Integer getRarityOrdinal() {
        return this.getRarity().getOrdinal();
    }

    Integer getLevel();

    Double getValue();

}
