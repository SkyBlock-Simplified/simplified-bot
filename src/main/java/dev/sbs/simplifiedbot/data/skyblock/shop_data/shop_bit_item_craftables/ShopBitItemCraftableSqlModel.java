package dev.sbs.simplifiedbot.data.skyblock.shop_data.shop_bit_item_craftables;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.shop_data.shop_bit_items.ShopBitItemSqlModel;
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
    name = "skyblock_shop_bit_item_craftables",
    indexes = {
        @Index(
            columnList = "bit_item_id"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ShopBitItemCraftableSqlModel implements ShopBitItemCraftableModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private ItemSqlModel craftableItem;

    @Setter
    @ManyToOne
    @JoinColumn(name = "bit_item_id", referencedColumnName = "item_id", nullable = false)
    private ShopBitItemSqlModel bitItem;

    @Setter
    @Column(name = "description", nullable = false)
    private String description;

    @Setter
    @Column(name = "expression", nullable = false)
    private String expression;

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

        ShopBitItemCraftableSqlModel that = (ShopBitItemCraftableSqlModel) o;

        return new EqualsBuilder()
            .append(this.getBitItem(), that.getBitItem())
            .append(this.getCraftableItem(), that.getCraftableItem())
            .append(this.getDescription(), that.getDescription())
            .append(this.getExpression(), that.getExpression())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getBitItem())
            .append(this.getCraftableItem())
            .append(this.getDescription())
            .append(this.getExpression())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
