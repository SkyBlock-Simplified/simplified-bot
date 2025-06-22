package dev.sbs.simplifiedbot.data.skyblock.potion_data.potion_tiers;

import dev.sbs.api.data.model.BuffEffectsModel;
import dev.sbs.minecraftapi.text.ChatFormat;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;
import dev.sbs.simplifiedbot.data.skyblock.potion_data.potions.PotionModel;

public interface PotionTierModel extends BuffEffectsModel<Double, Double> {

    PotionModel getPotion();

    Integer getTier();

    default ChatFormat getChatFormatting() {
        if (this.getTier() >= 7)
            return ChatFormat.DARK_PURPLE;
        else if (this.getTier() >= 5)
            return ChatFormat.BLUE;
        else if (this.getTier() >= 3)
            return ChatFormat.GREEN;
        else
            return ChatFormat.WHITE;
    }

    ItemModel getIngredientItem();

    String getBaseItem();

    Integer getExperienceYield();

    Double getSellPrice();

}
