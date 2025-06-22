package dev.sbs.simplifiedbot.data.skyblock.hotm_perk_stats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.hotm_perks.HotmPerkSqlModel;
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
    name = "skyblock_hotm_perk_stats",
    indexes = {
        @Index(
            columnList = "perk_key, stat_key",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HotmPerkStatSqlModel implements HotmPerkStatModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "perk_key", referencedColumnName = "key")
    private HotmPerkSqlModel perk;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "stat_key", referencedColumnName = "key")
    private StatSqlModel stat;

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

        HotmPerkStatSqlModel that = (HotmPerkStatSqlModel) o;

        return new EqualsBuilder()
            .append(this.getPerk(), that.getPerk())
            .append(this.getStat(), that.getStat())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getPerk())
            .append(this.getStat())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
