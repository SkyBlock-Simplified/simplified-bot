package dev.sbs.simplifiedbot.data.skyblock.stats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.CacheExpiry;
import dev.sbs.api.data.sql.converter.UnicodeConverter;
import dev.sbs.simplifiedbot.data.skyblock.formats.FormatSqlModel;
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
import java.util.concurrent.TimeUnit;

@Getter
@Entity
@Table(
    name = "skyblock_stats",
    indexes = {
        @Index(
            columnList = "format_key"
        )
    }
)
@CacheExpiry(length = TimeUnit.HOURS, value = 24)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StatSqlModel implements StatModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "symbol_code", length = 4)
    @Convert(converter = UnicodeConverter.class)
    private char symbol;

    @Setter
    @ManyToOne
    @JoinColumn(name = "format_key", referencedColumnName = "key")
    private FormatSqlModel format;

    @Setter
    @Column(name = "multipliable")
    private boolean multiplicable;

    @Setter
    @Column(name = "tunable", nullable = false)
    private boolean tunable;

    @Setter
    @Column(name = "tuning_bonus", nullable = false)
    private Double tuningBonus;

    @Setter
    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;

    @Setter
    @Column(name = "base_value", nullable = false)
    private Integer baseValue;

    @Setter
    @Column(name = "max_value", nullable = false)
    private Integer maxValue;

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

        StatSqlModel that = (StatSqlModel) o;

        return new EqualsBuilder()
            .append(this.getSymbol(), that.getSymbol())
            .append(this.isMultiplicable(), that.isMultiplicable())
            .append(this.isTunable(), that.isTunable())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getFormat(), that.getFormat())
            .append(this.getTuningBonus(), that.getTuningBonus())
            .append(this.getOrdinal(), that.getOrdinal())
            .append(this.getBaseValue(), that.getBaseValue())
            .append(this.getMaxValue(), that.getMaxValue())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getSymbol())
            .append(this.getFormat())
            .append(this.isMultiplicable())
            .append(this.isTunable())
            .append(this.getTuningBonus())
            .append(this.getOrdinal())
            .append(this.getBaseValue())
            .append(this.getMaxValue())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
