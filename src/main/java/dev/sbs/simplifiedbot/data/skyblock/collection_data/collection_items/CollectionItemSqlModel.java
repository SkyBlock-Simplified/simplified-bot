package dev.sbs.simplifiedbot.data.skyblock.collection_data.collection_items;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.collection_data.collections.CollectionSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
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
    name = "skyblock_collection_items",
    indexes = {
        @Index(
            columnList = "collection_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CollectionItemSqlModel implements CollectionItemModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private ItemSqlModel item;

    @Setter
    @ManyToOne
    @JoinColumn(name = "collection_key", referencedColumnName = "key", nullable = false)
    private CollectionSqlModel collection;

    @Setter
    @Column(name = "max_tiers", nullable = false)
    private Integer maxTiers;

    @Setter
    @Column(name = "farming_event", nullable = false)
    private boolean farmingEvent;

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

        CollectionItemSqlModel that = (CollectionItemSqlModel) o;

        return new EqualsBuilder()
            .append(this.isFarmingEvent(), that.isFarmingEvent())
            .append(this.getCollection(), that.getCollection())
            .append(this.getItem(), that.getItem())
            .append(this.getMaxTiers(), that.getMaxTiers())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getCollection())
            .append(this.getItem())
            .append(this.getMaxTiers())
            .append(this.isFarmingEvent())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
