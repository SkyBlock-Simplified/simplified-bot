package dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforge_stats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RaritySqlModel;
import dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforges.ReforgeSqlModel;
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
    name = "skyblock_reforge_stats",
    indexes = {
        @Index(
            columnList = "reforge_key, rarity_key",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ReforgeStatSqlModel implements ReforgeStatModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "reforge_key", referencedColumnName = "key")
    private ReforgeSqlModel reforge;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "rarity_key", referencedColumnName = "key")
    private RaritySqlModel rarity;

    @Setter
    @Column(name = "effects")
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> effects;

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

        ReforgeStatSqlModel that = (ReforgeStatSqlModel) o;

        return new EqualsBuilder()
            .append(this.getReforge(), that.getReforge())
            .append(this.getRarity(), that.getRarity())
            .append(this.getEffects(), that.getEffects())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getReforge())
            .append(this.getRarity())
            .append(this.getEffects())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
