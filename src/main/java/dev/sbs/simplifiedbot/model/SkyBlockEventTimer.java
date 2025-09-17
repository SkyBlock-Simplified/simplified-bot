package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;
import dev.sbs.minecraftapi.skyblock.date.Season;

public interface SkyBlockEventTimer extends Model {

    SkyBlockEvent getEvent();

    Season getStart();

    Integer getStartDay();

    Season getEnd();

    Integer getEndDay();

}
