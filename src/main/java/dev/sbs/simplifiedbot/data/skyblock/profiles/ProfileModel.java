package dev.sbs.simplifiedbot.data.skyblock.profiles;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiModel;

public interface ProfileModel extends Model {

    String getKey();

    String getName();

    EmojiModel getEmoji();

}
