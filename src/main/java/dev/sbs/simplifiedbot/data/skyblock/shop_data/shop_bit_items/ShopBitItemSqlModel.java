package dev.sbs.simplifiedbot.data.skyblock.shop_data.shop_bit_items;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.shop_data.shop_bit_types.ShopBitTypeSqlModel;
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
    name = "skyblock_shop_bit_items",
    indexes = {
        @Index(
            columnList = "bit_type_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ShopBitItemSqlModel implements ShopBitItemModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private ItemSqlModel item;

    @Setter
    @ManyToOne
    @JoinColumn(name = "bit_type_key", referencedColumnName = "key", nullable = false)
    private ShopBitTypeSqlModel type;

    @Setter
    @Column(name = "bit_cost", nullable = false)
    private Integer bitCost;

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

        ShopBitItemSqlModel that = (ShopBitItemSqlModel) o;

        return new EqualsBuilder()
            .append(this.getItem(), that.getItem())
            .append(this.getType(), that.getType())
            .append(this.getBitCost(), that.getBitCost())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getItem())
            .append(this.getType())
            .append(this.getBitCost())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
