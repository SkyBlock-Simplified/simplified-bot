package dev.sbs.simplifiedbot.data.skyblock.craftingtable_data.craftingtable_recipe_slots;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.craftingtable_data.craftingtable_recipes.CraftingTableRecipeModel;
import dev.sbs.simplifiedbot.data.skyblock.craftingtable_data.craftingtable_slots.CraftingTableSlotModel;

public interface CraftingTableRecipeSlotModel extends Model {

    CraftingTableRecipeModel getRecipe();

    CraftingTableSlotModel getSlot();

    Integer getOrdinal();

}
