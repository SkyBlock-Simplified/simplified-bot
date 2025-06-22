package dev.sbs.simplifiedbot.data.skyblock.bonus_data.bonus_enchantment_stats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import dev.sbs.api.data.sql.converter.map.StringObjectMapConverter;
import dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantments.EnchantmentSqlModel;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Map;

@Getter
@Entity
@Table(
    name = "skyblock_bonus_enchantment_stats"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BonusEnchantmentStatSqlModel implements BonusEnchantmentStatModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "enchantment_key", referencedColumnName = "key")
    private EnchantmentSqlModel enchantment;

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

        BonusEnchantmentStatSqlModel that = (BonusEnchantmentStatSqlModel) o;

        return new EqualsBuilder()
            .append(this.getEnchantment(), that.getEnchantment())
            .append(this.getEffects(), that.getEffects())
            .append(this.getBuffEffects(), that.getBuffEffects())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getEnchantment())
            .append(this.getEffects())
            .append(this.getBuffEffects())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
