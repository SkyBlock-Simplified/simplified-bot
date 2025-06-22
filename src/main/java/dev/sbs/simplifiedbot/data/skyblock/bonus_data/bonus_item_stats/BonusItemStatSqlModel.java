package dev.sbs.simplifiedbot.data.skyblock.bonus_data.bonus_item_stats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import dev.sbs.api.data.sql.converter.map.StringObjectMapConverter;
import dev.sbs.simplifiedbot.data.discord.optimizer_mob_types.OptimizerMobTypeSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.Map;

@Getter
@Entity
@Table(
    name = "skyblock_bonus_item_stats",
    indexes = {
        @Index(
            columnList = "item_id, stats, reforges, gems, required_mob_type_key",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BonusItemStatSqlModel implements BonusItemStatModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", nullable = false)
    private ItemSqlModel item;

    @Setter
    @Column(name = "stats", nullable = false)
    private boolean forStats;

    @Setter
    @Column(name = "reforges", nullable = false)
    private boolean forReforges;

    @Setter
    @Column(name = "gems", nullable = false)
    private boolean forGems;

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

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BonusItemStatSqlModel that = (BonusItemStatSqlModel) o;

        return new EqualsBuilder()
            .append(this.isForStats(), that.isForStats())
            .append(this.isForReforges(), that.isForReforges())
            .append(this.isForGems(), that.isForGems())
            .append(this.getId(), that.getId())
            .append(this.getItem(), that.getItem())
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
            .append(this.getItem())
            .append(this.isForStats())
            .append(this.isForReforges())
            .append(this.isForGems())
            .append(this.getRequiredMobType())
            .append(this.getEffects())
            .append(this.getBuffEffects())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
