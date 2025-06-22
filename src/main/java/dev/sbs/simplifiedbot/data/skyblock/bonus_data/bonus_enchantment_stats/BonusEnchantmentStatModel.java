package dev.sbs.simplifiedbot.data.skyblock.bonus_data.bonus_enchantment_stats;

import dev.sbs.api.data.model.BuffEffectsModel;
import dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantments.EnchantmentModel;

public interface BonusEnchantmentStatModel extends BuffEffectsModel<Object, Double> {

    EnchantmentModel getEnchantment();

}
