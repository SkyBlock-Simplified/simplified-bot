package dev.sbs.simplifiedbot.data.skyblock.npcs;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.location_data.location_areas.LocationAreaModel;
import dev.sbs.simplifiedbot.data.skyblock.location_data.locations.LocationModel;

public interface NpcModel extends Model {

    Double getX();

    Double getY();

    Double getZ();

    String getName();

    LocationModel getLocation();

    LocationAreaModel getLocationArea();

}
