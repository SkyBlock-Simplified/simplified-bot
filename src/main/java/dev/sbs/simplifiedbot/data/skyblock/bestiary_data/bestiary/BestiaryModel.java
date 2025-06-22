package dev.sbs.simplifiedbot.data.skyblock.bestiary_data.bestiary;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.bestiary_data.bestiary_brackets.BestiaryBracketModel;
import dev.sbs.simplifiedbot.data.skyblock.bestiary_data.bestiary_categories.BestiaryCategoryModel;

import java.util.List;

public interface BestiaryModel extends Model {

    String getKey();

    String getName();

    BestiaryBracketModel getBracket();

    BestiaryCategoryModel getCategory();

    Integer getOrdinal();

    String getInternalPattern();

    List<Integer> getLevels();

}
