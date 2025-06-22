package dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantments;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantment_families.EnchantmentFamilyModel;

import java.util.List;

public interface EnchantmentModel extends Model {

    String getKey();

    String getName();

    EnchantmentFamilyModel getFamily();

    String getDescription();

    List<String> getMobTypes();

    Integer getRequiredLevel();

}
