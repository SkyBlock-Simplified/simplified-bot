package dev.sbs.simplifiedbot.data.skyblock.bestiary_data.bestiary_categories;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.location_data.locations.LocationModel;

public interface BestiaryCategoryModel extends Model {

    String getKey();

    String getName();

    LocationModel getLocation();

    Integer getOrdinal();

}
