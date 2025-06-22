package dev.sbs.simplifiedbot.data.skyblock.minion_data.minion_items;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.collection_data.collection_items.CollectionItemSqlModel;
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
    name = "skyblock_minion_items",
    indexes = {
        @Index(
            columnList = "minion_key"
        ),
        @Index(
            columnList = "collection_item_id"
        ),
        @Index(
            columnList = "item_id"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MinionItemSqlModel implements MinionItemModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "minion_key", referencedColumnName = "key")
    private MinionSqlModel minion;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private ItemSqlModel item;

    @Setter
    @ManyToOne
    @JoinColumn(name = "collection_item_id", referencedColumnName = "item_id")
    private CollectionItemSqlModel collectionItem;

    @Setter
    @Column(name = "average_yield", nullable = false)
    private Double averageYield;

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

        MinionItemSqlModel that = (MinionItemSqlModel) o;

        return new EqualsBuilder()
            .append(this.getMinion(), that.getMinion())
            .append(this.getCollectionItem(), that.getCollectionItem())
            .append(this.getItem(), that.getItem())
            .append(this.getAverageYield(), that.getAverageYield())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getMinion())
            .append(this.getCollectionItem())
            .append(this.getItem())
            .append(this.getAverageYield())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
