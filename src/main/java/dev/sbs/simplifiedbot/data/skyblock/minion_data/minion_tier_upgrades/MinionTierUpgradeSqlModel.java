package dev.sbs.simplifiedbot.data.skyblock.minion_data.minion_tier_upgrades;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.minion_data.minion_tiers.MinionTierSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "skyblock_minion_tier_upgrades",
    indexes = {
        @Index(
            columnList = "minion_tier, item_cost",
            unique = true
        ),
        @Index(
            columnList = "minion_tier"
        ),
        @Index(
            columnList = "item_cost"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MinionTierUpgradeSqlModel implements MinionTierUpgradeModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "minion_tier", referencedColumnName = "tier", nullable = false)
    private MinionTierSqlModel minionTier;

    @Setter
    @Column(name = "coin_cost", nullable = false)
    private Double coinCost;

    @Setter
    @ManyToOne
    @JoinColumn(name = "item_cost", referencedColumnName = "item_id")
    private ItemSqlModel itemCost;

    @Setter
    @Column(name = "item_quantity", nullable = false)
    private Integer itemQuantity;

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

        MinionTierUpgradeSqlModel that = (MinionTierUpgradeSqlModel) o;

        return new EqualsBuilder()
            .append(this.getId(), that.getId())
            .append(this.getMinionTier(), that.getMinionTier())
            .append(this.getCoinCost(), that.getCoinCost())
            .append(this.getItemCost(), that.getItemCost())
            .append(this.getItemQuantity(), that.getItemQuantity())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getMinionTier())
            .append(this.getCoinCost())
            .append(this.getItemCost())
            .append(this.getItemQuantity())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
