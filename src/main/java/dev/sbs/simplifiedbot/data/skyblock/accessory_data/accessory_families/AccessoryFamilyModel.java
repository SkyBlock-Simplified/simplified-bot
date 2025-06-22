package dev.sbs.simplifiedbot.data.skyblock.accessory_data.accessory_families;

import dev.sbs.api.data.model.Model;

public interface AccessoryFamilyModel extends Model {

    String getKey();

    String getName();

    boolean isReforgesStackable();

    boolean isStatsStackable();

}
