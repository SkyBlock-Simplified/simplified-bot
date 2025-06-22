package dev.sbs.simplifiedbot.data.skyblock.items;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.list.StringObjectMapListConverter;
import dev.sbs.api.data.sql.converter.list.StringObjectMapListListConverter;
import dev.sbs.api.data.sql.converter.map.StringDoubleListMapConverter;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import dev.sbs.api.data.sql.converter.map.StringObjectMapConverter;
import dev.sbs.simplifiedbot.data.skyblock.item_types.ItemTypeSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RaritySqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Entity
@Table(
    name = "skyblock_items",
    indexes = {
        @Index(
            columnList = "generator"
        ),
        @Index(
            columnList = "rarity_key"
        ),
        @Index(
            columnList = "item_type_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ItemSqlModel implements ItemModel, SqlModel {

    @Id
    @Setter
    @Column(name = "item_id")
    private String itemId;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "material")
    private String material;

    @Setter
    @Column(name = "durability", nullable = false)
    private Integer durability;

    @Setter
    @Column(name = "description")
    private String description;

    @Setter
    @ManyToOne
    @JoinColumn(name = "rarity_key", referencedColumnName = "key", nullable = false)
    private RaritySqlModel rarity;

    @Setter
    @ManyToOne
    @JoinColumn(name = "item_type_key", referencedColumnName = "key")
    private ItemTypeSqlModel type;

    @Setter
    @Column(name = "color", length = 31)
    private String color;

    @Setter
    @Column(name = "obtainable", nullable = false)
    private boolean obtainable;

    @Setter
    @Column(name = "glowing", nullable = false)
    private boolean glowing;

    @Setter
    @Column(name = "unstackable", nullable = false)
    private boolean unstackable;

    @Setter
    @Column(name = "special_museum", nullable = false)
    private boolean inSpecialMuseum;

    @Setter
    @Column(name = "dungeon_item", nullable = false)
    private boolean dungeonItem;

    @Setter
    @Column(name = "attributable", nullable = false)
    private boolean attributable;

    @Setter
    @Column(name = "hidden_from_viewrecipe", nullable = false)
    private boolean hiddenFromViewrecipe;

    @Setter
    @Column(name = "salvageable_from_recipe", nullable = false)
    private boolean salvageableFromRecipe;

    @Setter
    @Column(name = "not_reforgeable", nullable = false)
    private boolean notReforgeable;

    @Setter
    @Column(name = "rift_transferrable", nullable = false)
    private boolean riftTransferrable;

    @Setter
    @Column(name = "rift_lose_motes_value_on_transfer", nullable = false)
    private boolean riftLoseMotesValueOnTransfer;

    @Setter
    @Column(name = "rift_motes_sell_price", nullable = false)
    private Double riftMotesSellPrice;

    @Setter
    @Column(name = "npc_sell_price", nullable = false)
    private Double npcSellPrice;

    @Setter
    @Column(name = "gear_score", nullable = false)
    private Integer gearScore;

    @Setter
    @Column(name = "generator")
    private String generator;

    @Setter
    @Column(name = "generator_tier", nullable = false)
    private Integer generatorTier;

    @Setter
    @Column(name = "ability_damage_scaling", nullable = false)
    private Double abilityDamageScaling;

    @Setter
    @Column(name = "origin", length = 10)
    private String origin;

    @Setter
    @Column(name = "soulbound", length = 10)
    private String soulbound;

    @Setter
    @Column(name = "furniture")
    private String furniture;

    @Setter
    @Column(name = "sword_type")
    private String swordType;

    @Setter
    @Column(name = "skin", length = 1023)
    private String skin;

    @Setter
    @Column(name = "crystal")
    private String crystal;

    @Setter
    @Column(name = "private_island")
    private String privateIsland;

    @Setter
    @Column(name = "stats", nullable = false)
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> stats;

    @Setter
    @Column(name = "tiered_stats", nullable = false)
    @Convert(converter = StringDoubleListMapConverter.class)
    private Map<String, List<Double>> tieredStats;

    @Setter
    @Column(name = "requirements", nullable = false)
    @Convert(converter = StringObjectMapListConverter.class)
    private List<Map<String, Object>> requirements;

    @Setter
    @Column(name = "catacombs_requirements", nullable = false)
    @Convert(converter = StringObjectMapListConverter.class)
    private List<Map<String, Object>> catacombsRequirements;

    @Setter
    @Column(name = "upgrade_costs", nullable = false)
    @Convert(converter = StringObjectMapListListConverter.class)
    private List<List<Map<String, Object>>> upgradeCosts;

    @Setter
    @Column(name = "gemstone_slots", nullable = false)
    @Convert(converter = StringObjectMapListConverter.class)
    private List<Map<String, Object>> gemstoneSlots;

    @Setter
    @Column(name = "dungeon_item_conversion_cost", nullable = false)
    @Convert(converter = StringObjectMapConverter.class)
    private Map<String, Object> dungeonItemConversionCost;

    @Setter
    @Column(name = "enchantments", nullable = false)
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> enchantments;

    @Setter
    @Column(name = "prestige", nullable = false)
    @Convert(converter = StringObjectMapConverter.class)
    private Map<String, Object> prestige;

    @Setter
    @Column(name = "salvages", nullable = false)
    @Convert(converter = StringObjectMapListConverter.class)
    private List<Map<String, Object>> salvages;

    @Setter
    @Column(name = "item_specific", nullable = false)
    @Convert(converter = StringObjectMapConverter.class)
    private Map<String, Object> itemSpecific;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @UpdateTimestamp
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemSqlModel that = (ItemSqlModel) o;

        return new EqualsBuilder()
            .append(this.isObtainable(), that.isObtainable())
            .append(this.isGlowing(), that.isGlowing())
            .append(this.isUnstackable(), that.isUnstackable())
            .append(this.isInSpecialMuseum(), that.isInSpecialMuseum())
            .append(this.isDungeonItem(), that.isDungeonItem())
            .append(this.isAttributable(), that.isAttributable())
            .append(this.isHiddenFromViewrecipe(), that.isHiddenFromViewrecipe())
            .append(this.isSalvageableFromRecipe(), that.isSalvageableFromRecipe())
            .append(this.isNotReforgeable(), that.isNotReforgeable())
            .append(this.isRiftTransferrable(), that.isRiftTransferrable())
            .append(this.isRiftLoseMotesValueOnTransfer(), that.isRiftLoseMotesValueOnTransfer())
            .append(this.getItemId(), that.getItemId())
            .append(this.getName(), that.getName())
            .append(this.getMaterial(), that.getMaterial())
            .append(this.getDurability(), that.getDurability())
            .append(this.getDescription(), that.getDescription())
            .append(this.getRarity(), that.getRarity())
            .append(this.getType(), that.getType())
            .append(this.getColor(), that.getColor())
            .append(this.getRiftMotesSellPrice(), that.getRiftMotesSellPrice())
            .append(this.getNpcSellPrice(), that.getNpcSellPrice())
            .append(this.getGearScore(), that.getGearScore())
            .append(this.getGenerator(), that.getGenerator())
            .append(this.getGeneratorTier(), that.getGeneratorTier())
            .append(this.getAbilityDamageScaling(), that.getAbilityDamageScaling())
            .append(this.getOrigin(), that.getOrigin())
            .append(this.getSoulbound(), that.getSoulbound())
            .append(this.getFurniture(), that.getFurniture())
            .append(this.getSwordType(), that.getSwordType())
            .append(this.getSkin(), that.getSkin())
            .append(this.getCrystal(), that.getCrystal())
            .append(this.getPrivateIsland(), that.getPrivateIsland())
            .append(this.getStats(), that.getStats())
            .append(this.getTieredStats(), that.getTieredStats())
            .append(this.getRequirements(), that.getRequirements())
            .append(this.getCatacombsRequirements(), that.getCatacombsRequirements())
            .append(this.getUpgradeCosts(), that.getUpgradeCosts())
            .append(this.getGemstoneSlots(), that.getGemstoneSlots())
            .append(this.getDungeonItemConversionCost(), that.getDungeonItemConversionCost())
            .append(this.getEnchantments(), that.getEnchantments())
            .append(this.getPrestige(), that.getPrestige())
            .append(this.getSalvages(), that.getSalvages())
            .append(this.getItemSpecific(), that.getItemSpecific())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getItemId())
            .append(this.getName())
            .append(this.getMaterial())
            .append(this.getDurability())
            .append(this.getDescription())
            .append(this.getRarity())
            .append(this.getType())
            .append(this.getColor())
            .append(this.isObtainable())
            .append(this.isGlowing())
            .append(this.isUnstackable())
            .append(this.isInSpecialMuseum())
            .append(this.isDungeonItem())
            .append(this.isAttributable())
            .append(this.isHiddenFromViewrecipe())
            .append(this.isSalvageableFromRecipe())
            .append(this.isNotReforgeable())
            .append(this.isRiftTransferrable())
            .append(this.isRiftLoseMotesValueOnTransfer())
            .append(this.getRiftMotesSellPrice())
            .append(this.getNpcSellPrice())
            .append(this.getGearScore())
            .append(this.getGenerator())
            .append(this.getGeneratorTier())
            .append(this.getAbilityDamageScaling())
            .append(this.getOrigin())
            .append(this.getSoulbound())
            .append(this.getFurniture())
            .append(this.getSwordType())
            .append(this.getSkin())
            .append(this.getCrystal())
            .append(this.getPrivateIsland())
            .append(this.getStats())
            .append(this.getTieredStats())
            .append(this.getRequirements())
            .append(this.getCatacombsRequirements())
            .append(this.getUpgradeCosts())
            .append(this.getGemstoneSlots())
            .append(this.getDungeonItemConversionCost())
            .append(this.getEnchantments())
            .append(this.getPrestige())
            .append(this.getSalvages())
            .append(this.getItemSpecific())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
