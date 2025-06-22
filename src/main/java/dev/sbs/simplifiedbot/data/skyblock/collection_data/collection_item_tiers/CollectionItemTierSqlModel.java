package dev.sbs.simplifiedbot.data.skyblock.collection_data.collection_item_tiers;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.list.StringListConverter;
import dev.sbs.simplifiedbot.data.skyblock.collection_data.collection_items.CollectionItemSqlModel;
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
import java.util.List;

@Getter
@Entity
@Table(
    name = "skyblock_collection_item_tiers",
    indexes = {
        @Index(
            columnList = "collection_item_id, tier",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CollectionItemTierSqlModel implements CollectionItemTierModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "collection_item_id", referencedColumnName = "item_id")
    private CollectionItemSqlModel collectionItem;

    @Id
    @Setter
    @Column(name = "tier")
    private Integer tier;

    @Setter
    @Column(name = "amount_required", nullable = false)
    private Double amountRequired;

    @Setter
    @Column(name = "unlocks", nullable = false)
    @Convert(converter = StringListConverter.class)
    private List<String> unlocks;

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

        CollectionItemTierSqlModel that = (CollectionItemTierSqlModel) o;

        return new EqualsBuilder()
            .append(this.getCollectionItem(), that.getCollectionItem())
            .append(this.getTier(), that.getTier())
            .append(this.getAmountRequired(), that.getAmountRequired())
            .append(this.getUnlocks(), that.getUnlocks())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getCollectionItem())
            .append(this.getTier())
            .append(this.getAmountRequired())
            .append(this.getUnlocks())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}

