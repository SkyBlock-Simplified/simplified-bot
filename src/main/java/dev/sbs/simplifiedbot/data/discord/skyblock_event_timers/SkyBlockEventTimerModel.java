package dev.sbs.simplifiedbot.data.discord.skyblock_event_timers;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.skyblock_events.SkyBlockEventModel;
import dev.sbs.simplifiedbot.data.skyblock.seasons.SeasonModel;

public interface SkyBlockEventTimerModel extends Model {

    SkyBlockEventModel getEvent();

    SeasonModel getStart();

    Integer getStartDay();

    SeasonModel getEnd();

    Integer getEndDay();

}
