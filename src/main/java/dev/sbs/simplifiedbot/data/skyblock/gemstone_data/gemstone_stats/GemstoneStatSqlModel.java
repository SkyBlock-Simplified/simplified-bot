package dev.sbs.simplifiedbot.data.skyblock.gemstone_data.gemstone_stats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.gemstone_data.gemstone_types.GemstoneTypeSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.gemstone_data.gemstones.GemstoneSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RaritySqlModel;
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
    name = "skyblock_gemstone_stats",
    indexes = {
        @Index(
            columnList = "gemstone_key, type_key, rarity_key",
            unique = true
        ),
        @Index(
            columnList = "type_key"
        ),
        @Index(
            columnList = "rarity_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GemstoneStatSqlModel implements GemstoneStatModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "gemstone_key", referencedColumnName = "key")
    private GemstoneSqlModel gemstone;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "type_key", referencedColumnName = "key")
    private GemstoneTypeSqlModel type;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "rarity_key", referencedColumnName = "key")
    private RaritySqlModel rarity;

    @Setter
    @Column(name = "value", nullable = false)
    private Double value;

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

        GemstoneStatSqlModel that = (GemstoneStatSqlModel) o;

        return new EqualsBuilder()
            .append(this.getGemstone(), that.getGemstone())
            .append(this.getType(), that.getType())
            .append(this.getRarity(), that.getRarity())
            .append(this.getValue(), that.getValue())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getGemstone())
            .append(this.getType())
            .append(this.getRarity())
            .append(this.getValue())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
