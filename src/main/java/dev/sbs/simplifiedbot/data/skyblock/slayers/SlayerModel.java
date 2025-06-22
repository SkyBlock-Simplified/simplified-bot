package dev.sbs.simplifiedbot.data.skyblock.slayers;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiModel;

public interface SlayerModel extends Model {

    String getKey();

    String getName();

    EmojiModel getEmoji();

    Double getWeightDivider();

    Double getWeightModifier();

}
