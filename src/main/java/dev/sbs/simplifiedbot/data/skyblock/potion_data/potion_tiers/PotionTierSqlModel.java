package dev.sbs.simplifiedbot.data.skyblock.potion_data.potion_tiers;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.potion_data.potions.PotionSqlModel;
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
import java.util.Map;

@Getter
@Entity
@Table(
    name = "skyblock_potion_tiers",
    indexes = {
        @Index(
            columnList = "potion_key, tier",
            unique = true
        ),
        @Index(
            columnList = "tier"
        ),
        @Index(
            columnList = "ingredient_item_id"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PotionTierSqlModel implements PotionTierModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "potion_key", referencedColumnName = "key")
    private PotionSqlModel potion;

    @Id
    @Setter
    @Column(name = "tier")
    private Integer tier;

    @Setter
    @ManyToOne
    @JoinColumn(name = "ingredient_item_id", referencedColumnName = "item_id")
    private ItemSqlModel ingredientItem;

    @Setter
    @Column(name = "base_item_id")
    private String baseItem;

    @Setter
    @Column(name = "exp_yield", nullable = false)
    private Integer experienceYield;

    @Setter
    @Column(name = "sell_price", nullable = false)
    private Double sellPrice;

    @Setter
    @Column(name = "effects", nullable = false)
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> effects;

    @Setter
    @Column(name = "buff_effects", nullable = false)
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> buffEffects;

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

        PotionTierSqlModel that = (PotionTierSqlModel) o;

        return new EqualsBuilder()
            .append(this.getPotion(), that.getPotion())
            .append(this.getTier(), that.getTier())
            .append(this.getIngredientItem(), that.getIngredientItem())
            .append(this.getBaseItem(), that.getBaseItem())
            .append(this.getExperienceYield(), that.getExperienceYield())
            .append(this.getSellPrice(), that.getSellPrice())
            .append(this.getEffects(), that.getEffects())
            .append(this.getBuffEffects(), that.getBuffEffects())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getPotion())
            .append(this.getTier())
            .append(this.getIngredientItem())
            .append(this.getBaseItem())
            .append(this.getExperienceYield())
            .append(this.getSellPrice())
            .append(this.getEffects())
            .append(this.getBuffEffects())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
