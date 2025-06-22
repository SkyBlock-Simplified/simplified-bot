package dev.sbs.simplifiedbot.data.skyblock.bag_sizes;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.bags.BagSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
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
    name = "skyblock_bag_sizes",
    indexes = {
        @Index(
            columnList = "bag_key, collection_tier",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BagSizeSqlModel implements BagSizeModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "bag_key", referencedColumnName = "key")
    private BagSqlModel bag;

    @Id
    @Setter
    @Column(name = "collection_tier")
    private Integer collectionTier;

    @Setter
    @Column(name = "slot_count", nullable = false)
    private Integer slotCount;

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

        BagSizeSqlModel that = (BagSizeSqlModel) o;

        return new EqualsBuilder()
            .append(this.getBag(), that.getBag())
            .append(this.getCollectionTier(), that.getCollectionTier())
            .append(this.getSlotCount(), that.getSlotCount())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getBag())
            .append(this.getCollectionTier())
            .append(this.getSlotCount())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
