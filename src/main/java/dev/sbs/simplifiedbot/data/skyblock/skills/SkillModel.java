package dev.sbs.simplifiedbot.data.skyblock.skills;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;

public interface SkillModel extends Model {

    String getKey();

    String getName();

    String getDescription();

    Integer getMaxLevel();

    ItemModel getItem();

    EmojiModel getEmoji();

    boolean isCosmetic();

    Double getWeightExponent();

    Double getWeightDivider();

}
