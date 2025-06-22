package dev.sbs.simplifiedbot.data.skyblock.minion_data.minion_tiers;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.minion_data.minions.MinionSqlModel;
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
    name = "skyblock_minion_tiers",
    indexes = {
        @Index(
            columnList = "tier, minion_key",
            unique = true
        ),
        @Index(
            columnList = "minion_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MinionTierSqlModel implements MinionTierModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "tier", referencedColumnName = "item_id")
    private ItemSqlModel item;

    @Setter
    @ManyToOne
    @JoinColumn(name = "minion_key", referencedColumnName = "key")
    private MinionSqlModel minion;

    @Setter
    @Column(name = "speed", nullable = false)
    private Integer speed;

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

        MinionTierSqlModel that = (MinionTierSqlModel) o;

        return new EqualsBuilder()
            .append(this.getMinion(), that.getMinion())
            .append(this.getItem(), that.getItem())
            .append(this.getSpeed(), that.getSpeed())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getMinion())
            .append(this.getItem())
            .append(this.getSpeed())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
