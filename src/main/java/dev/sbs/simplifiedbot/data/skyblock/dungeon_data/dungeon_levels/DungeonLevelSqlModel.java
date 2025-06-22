package dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeon_levels;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.CacheExpiry;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@Entity
@Table(
    name = "skyblock_dungeon_levels"
)
@CacheExpiry(length = TimeUnit.HOURS, value = 24)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DungeonLevelSqlModel implements DungeonLevelModel, SqlModel {

    @Id
    @Setter
    @Column(name = "level")
    private Integer level;

    @Setter
    @Column(name = "total_exp_required", nullable = false)
    private Double totalExpRequired;

    @Setter
    @Column(name = "stat_multiplier", nullable = false)
    private Integer statMultiplier;

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

        DungeonLevelSqlModel that = (DungeonLevelSqlModel) o;

        return new EqualsBuilder()
            .append(this.getLevel(), that.getLevel())
            .append(this.getTotalExpRequired(), that.getTotalExpRequired())
            .append(this.getStatMultiplier(), that.getStatMultiplier())
            .append(this.getEffects(), that.getEffects())
            .append(this.getBuffEffects(), that.getBuffEffects())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getLevel())
            .append(this.getTotalExpRequired())
            .append(this.getStatMultiplier())
            .append(this.getEffects())
            .append(this.getBuffEffects())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
