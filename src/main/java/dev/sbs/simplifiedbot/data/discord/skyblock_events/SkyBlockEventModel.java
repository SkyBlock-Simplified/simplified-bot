package dev.sbs.simplifiedbot.data.discord.skyblock_events;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiModel;

public interface SkyBlockEventModel extends Model {

    String getKey();

    String getName();

    EmojiModel getBotEmoji();

    String getDescription();

    boolean isEnabled();

    String getStatus();

    String getIntervalExpression();

    String getThirdPartyJsonUrl();

}
