package dev.sbs.simplifiedbot.data.skyblock.trophy_fishes;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.location_data.location_areas.LocationAreaModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RarityModel;

public interface TrophyFishModel extends Model {

    String getKey();

    String getName();

    RarityModel getRarity();

    LocationAreaModel getLocationArea();

}
