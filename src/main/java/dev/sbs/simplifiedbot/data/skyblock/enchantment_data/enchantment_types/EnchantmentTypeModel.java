package dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantment_types;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantments.EnchantmentModel;

import java.util.List;

public interface EnchantmentTypeModel extends Model {

    EnchantmentModel getEnchantment();

    List<String> getItemTypes();

}
