package dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantment_stats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.list.IntegerListConverter;
import dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantments.EnchantmentSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Getter
@Entity
@Table(
    name = "skyblock_enchantment_stats",
    indexes = {
        @Index(
            columnList = "enchantment_key, stat_key, buff_key, levels",
            unique = true
        ),
        @Index(
            columnList = "stat_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EnchantmentStatSqlModel implements EnchantmentStatModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "enchantment_key", referencedColumnName = "key", nullable = false)
    private EnchantmentSqlModel enchantment;

    @Setter
    @ManyToOne
    @JoinColumn(name = "stat_key", referencedColumnName = "key")
    private StatSqlModel stat;

    @Setter
    @Column(name = "buff_key")
    private String buffKey;

    @Setter
    @Column(name = "levels", nullable = false)
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> levels;

    @Setter
    @Column(name = "base_value", nullable = false)
    private Double baseValue;

    @Setter
    @Column(name = "level_bonus", nullable = false)
    private Double levelBonus;

    @Setter
    @Column(name = "percentage", nullable = false)
    private boolean percentage;

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

        EnchantmentStatSqlModel that = (EnchantmentStatSqlModel) o;

        return new EqualsBuilder()
            .append(this.isPercentage(), that.isPercentage())
            .append(this.getId(), that.getId())
            .append(this.getEnchantment(), that.getEnchantment())
            .append(this.getStat(), that.getStat())
            .append(this.getBuffKey(), that.getBuffKey())
            .append(this.getLevels(), that.getLevels())
            .append(this.getBaseValue(), that.getBaseValue())
            .append(this.getLevelBonus(), that.getLevelBonus())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getEnchantment())
            .append(this.getStat())
            .append(this.getBuffKey())
            .append(this.getLevels())
            .append(this.getBaseValue())
            .append(this.getLevelBonus())
            .append(this.isPercentage())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
