package dev.sbs.simplifiedbot.data.skyblock.items;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.item_types.ItemTypeModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RarityModel;

import java.util.List;
import java.util.Map;

public interface ItemModel extends Model {

    String getItemId();

    String getName();

    String getMaterial();

    Integer getDurability();

    String getDescription();

    RarityModel getRarity();

    ItemTypeModel getType();

    String getColor();

    boolean isObtainable();

    default boolean isNotObtainable() {
        return !this.isObtainable();
    }

    boolean isGlowing();

    default boolean isNotGlowing() {
        return !this.isGlowing();
    }

    boolean isUnstackable();

    default boolean isStackable() {
        return !this.isUnstackable();
    }

    boolean isInSpecialMuseum();

    default boolean isNotInSpecialMuseum() {
        return !this.isInSpecialMuseum();
    }

    boolean isDungeonItem();

    default boolean isNotDungeonItem() {
        return !this.isDungeonItem();
    }

    boolean isAttributable();

    default boolean isNotAttributable() {
        return !this.isAttributable();
    }

    boolean isHiddenFromViewrecipe();

    default boolean isNotHiddenFromViewRecipe() {
        return !this.isHiddenFromViewrecipe();
    }

    boolean isSalvageableFromRecipe();

    default boolean isNotSalvageableFromRecipe() {
        return !this.isSalvageableFromRecipe();
    }

    boolean isNotReforgeable();

    default boolean isReforgeable() {
        return !this.isNotReforgeable();
    }

    boolean isRiftTransferrable();

    default boolean isNotRiftTransferrable() {
        return !this.isRiftTransferrable();
    }

    boolean isRiftLoseMotesValueOnTransfer();

    default boolean isNotRiftLoseMotesValueOnTransfer() {
        return !this.isRiftLoseMotesValueOnTransfer();
    }

    Double getRiftMotesSellPrice();

    Double getNpcSellPrice();

    Integer getGearScore();

    String getGenerator();

    Integer getGeneratorTier();

    Double getAbilityDamageScaling();

    String getOrigin();

    String getSoulbound();

    String getFurniture();

    String getSwordType();

    String getSkin();

    String getCrystal();

    String getPrivateIsland();

    Map<String, Double> getStats();

    Map<String, List<Double>> getTieredStats();

    List<Map<String, Object>> getRequirements();

    List<Map<String, Object>> getCatacombsRequirements();

    List<List<Map<String, Object>>> getUpgradeCosts();

    List<Map<String, Object>> getGemstoneSlots();

    Map<String, Double> getEnchantments();

    Map<String, Object> getDungeonItemConversionCost();

    Map<String, Object> getPrestige();

    Map<String, Object> getItemSpecific();

    List<Map<String, Object>> getSalvages();

}
