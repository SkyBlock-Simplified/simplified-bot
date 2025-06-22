package dev.sbs.simplifiedbot.data.skyblock.stats;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.formats.FormatModel;

public interface StatModel extends Model {

    String getKey();

    String getName();

    char getSymbol();

    FormatModel getFormat();

    boolean isMultiplicable();

    boolean isTunable();

    Double getTuningBonus();

    Integer getOrdinal();

    Integer getBaseValue();

    Integer getMaxValue();

}
