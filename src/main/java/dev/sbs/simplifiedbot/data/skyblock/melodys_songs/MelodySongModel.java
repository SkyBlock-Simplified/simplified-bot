package dev.sbs.simplifiedbot.data.skyblock.melodys_songs;

import dev.sbs.api.data.model.Model;

public interface MelodySongModel extends Model {

    String getKey();

    String getName();

    Integer getReward();

}
