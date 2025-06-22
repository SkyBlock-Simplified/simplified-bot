package dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantment_stats;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantments.EnchantmentModel;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatModel;

import java.util.List;

public interface EnchantmentStatModel extends Model {

    EnchantmentModel getEnchantment();

    StatModel getStat();

    String getBuffKey();

    List<Integer> getLevels();

    Double getBaseValue();

    Double getLevelBonus();

    boolean isPercentage();

    default boolean hasStat() {
        return this.getStat() != null;
    }

    default boolean notPercentage() {
        return !this.isPercentage();
    }

}
