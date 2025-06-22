package dev.sbs.simplifiedbot.data.skyblock.accessory_data.accessory_powers;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
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
    name = "skyblock_accessory_powers",
    indexes = {
        @Index(
            columnList = "item_id"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AccessoryPowerSqlModel implements AccessoryPowerModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private ItemSqlModel item;

    @Setter
    @Column(name = "min_combat_level", nullable = false)
    private Integer minimumCombatLevel;

    @Setter
    @Column(name = "effects", nullable = false)
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> effects;

    @Setter
    @Column(name = "unique_effects", nullable = false)
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> uniqueEffects;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessoryPowerSqlModel that = (AccessoryPowerSqlModel) o;

        return new EqualsBuilder()
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getItem(), that.getItem())
            .append(this.getMinimumCombatLevel(), that.getMinimumCombatLevel())
            .append(this.getEffects(), that.getEffects())
            .append(this.getUniqueEffects(), that.getUniqueEffects())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getItem())
            .append(this.getMinimumCombatLevel())
            .append(this.getEffects())
            .append(this.getUniqueEffects())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
