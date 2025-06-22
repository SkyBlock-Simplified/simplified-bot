package dev.sbs.simplifiedbot.data.skyblock.formats;

import dev.sbs.api.data.model.Model;
import dev.sbs.minecraftapi.text.ChatFormat;

import java.awt.*;

public interface FormatModel extends Model {

    String getKey();

    char getCode();

    Color getRgb();

    boolean isFormat();

    default ChatFormat getChatFormatting() {
        return ChatFormat.of(this.getKey());
    }

}
