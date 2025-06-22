package dev.sbs.simplifiedbot.data.skyblock.craftingtable_data.craftingtable_recipe_slots;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.craftingtable_data.craftingtable_recipes.CraftingTableRecipeSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.craftingtable_data.craftingtable_slots.CraftingTableSlotSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "skyblock_craftingtable_recipe_slots",
    indexes = {
        @Index(
            columnList = "recipe_key, slot_key",
            unique = true
        ),
        @Index(
            columnList = "recipe_key, ordinal",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CraftingTableRecipeSlotSqlModel implements CraftingTableRecipeSlotModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "recipe_key", referencedColumnName = "key")
    private CraftingTableRecipeSqlModel recipe;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "slot_key", referencedColumnName = "key")
    private CraftingTableSlotSqlModel slot;

    @Setter
    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;

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

        CraftingTableRecipeSlotSqlModel that = (CraftingTableRecipeSlotSqlModel) o;

        return new EqualsBuilder()
            .append(this.getRecipe(), that.getRecipe())
            .append(this.getSlot(), that.getSlot())
            .append(this.getOrdinal(), that.getOrdinal())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getRecipe())
            .append(this.getSlot())
            .append(this.getOrdinal())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
