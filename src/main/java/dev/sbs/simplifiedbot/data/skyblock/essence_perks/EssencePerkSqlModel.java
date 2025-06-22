package dev.sbs.simplifiedbot.data.skyblock.essence_perks;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatSqlModel;
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
    name = "skyblock_essence_perks",
    indexes = {
        @Index(
            columnList = "key, stat_key",
            unique = true
        ),
        @Index(
            columnList = "stat_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EssencePerkSqlModel implements EssencePerkModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @ManyToOne
    @JoinColumn(name = "stat_key", referencedColumnName = "key", nullable = false)
    private StatSqlModel stat;

    @Setter
    @Column(name = "level_bonus", nullable = false)
    private Integer levelBonus;

    @Setter
    @Column(name = "permanent", nullable = false)
    private boolean permanent;

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

        EssencePerkSqlModel that = (EssencePerkSqlModel) o;

        return new EqualsBuilder()
            .append(this.isPermanent(), that.isPermanent())
            .append(this.getKey(), that.getKey())
            .append(this.getStat(), that.getStat())
            .append(this.getLevelBonus(), that.getLevelBonus())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getStat())
            .append(this.getLevelBonus())
            .append(this.isPermanent())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
