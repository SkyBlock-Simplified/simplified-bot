package dev.sbs.simplifiedbot.data.skyblock.rarities;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiModel;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public interface RarityModel extends Model, Comparable<RarityModel> {

    @Override
    default int compareTo(@NotNull RarityModel o) {
        return Comparator.comparing(RarityModel::getOrdinal).compare(this, o);
    }

    String getKey();

    String getName();

    Integer getOrdinal();

    boolean isEnrichable();

    Integer getMagicPowerMultiplier();

    EmojiModel getEmoji();

}
