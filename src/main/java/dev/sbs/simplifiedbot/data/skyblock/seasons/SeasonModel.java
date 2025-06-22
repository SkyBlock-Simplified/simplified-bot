package dev.sbs.simplifiedbot.data.skyblock.seasons;

import dev.sbs.api.data.model.Model;
import dev.sbs.minecraftapi.util.SkyBlockDate;

public interface SeasonModel extends Model {

    String getKey();

    String getName();

    Integer getOrdinal();

    default SkyBlockDate.Season getSeason() {
        return SkyBlockDate.Season.valueOf(this.getKey());
    }

}
