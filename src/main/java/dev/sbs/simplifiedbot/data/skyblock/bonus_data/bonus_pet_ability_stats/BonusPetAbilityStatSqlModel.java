package dev.sbs.simplifiedbot.data.skyblock.bonus_data.bonus_pet_ability_stats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import dev.sbs.api.data.sql.converter.map.StringObjectMapConverter;
import dev.sbs.simplifiedbot.data.discord.optimizer_mob_types.OptimizerMobTypeSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_abilities.PetAbilitySqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.Map;

@Getter
@Entity
@Table(
    name = "skyblock_bonus_pet_ability_stats",
    indexes = {
        @Index(
            columnList = "ability_key, required_item_id, required_mob_type_key",
            unique = true
        ),
        @Index(
            columnList = "required_item_id"
        ),
        @Index(
            columnList = "required_mob_type_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BonusPetAbilityStatSqlModel implements BonusPetAbilityStatModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "ability_key", referencedColumnName = "key", nullable = false)
    private PetAbilitySqlModel petAbility;

    @Setter
    @Column(name = "percentage", nullable = false)
    private boolean percentage;

    @Setter
    @ManyToOne
    @JoinColumn(name = "required_item_id", referencedColumnName = "item_id")
    private ItemSqlModel requiredItem;

    @Setter
    @ManyToOne
    @JoinColumn(name = "required_mob_type_key", referencedColumnName = "key")
    private OptimizerMobTypeSqlModel requiredMobType;

    @Setter
    @Column(name = "effects", nullable = false)
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> effects;

    @Setter
    @Column(name = "buff_effects", nullable = false)
    @Convert(converter = StringObjectMapConverter.class)
    private Map<String, Object> buffEffects;

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

        BonusPetAbilityStatSqlModel that = (BonusPetAbilityStatSqlModel) o;

        return new EqualsBuilder()
            .append(this.isPercentage(), that.isPercentage())
            .append(this.getId(), that.getId())
            .append(this.getPetAbility(), that.getPetAbility())
            .append(this.getRequiredItem(), that.getRequiredItem())
            .append(this.getRequiredMobType(), that.getRequiredMobType())
            .append(this.getEffects(), that.getEffects())
            .append(this.getBuffEffects(), that.getBuffEffects())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getPetAbility())
            .append(this.isPercentage())
            .append(this.getRequiredItem())
            .append(this.getRequiredMobType())
            .append(this.getEffects())
            .append(this.getBuffEffects())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
