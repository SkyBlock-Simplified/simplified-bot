package dev.sbs.simplifiedbot.data.skyblock.shop_data.shop_bit_enchanted_books;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantments.EnchantmentSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "skyblock_shop_bit_enchanted_books"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ShopBitEnchantedBookSqlModel implements ShopBitEnchantedBookModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "enchantment_key", referencedColumnName = "key")
    private EnchantmentSqlModel enchantment;

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

        ShopBitEnchantedBookSqlModel that = (ShopBitEnchantedBookSqlModel) o;

        return new EqualsBuilder()
            .append(this.getEnchantment(), that.getEnchantment())
            .append(this.getBitCost(), that.getBitCost())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getEnchantment())
            .append(this.getBitCost())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
