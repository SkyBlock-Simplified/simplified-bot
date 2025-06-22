package dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantment_types;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.list.StringListConverter;
import dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantments.EnchantmentSqlModel;
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
    name = "skyblock_enchantment_types",
    indexes = {
        @Index(
            columnList = "enchantment_key",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EnchantmentTypeSqlModel implements EnchantmentTypeModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "enchantment_key", referencedColumnName = "key")
    private EnchantmentSqlModel enchantment;

    @Setter
    @Column(name = "item_types", nullable = false)
    @Convert(converter = StringListConverter.class)
    private List<String> itemTypes;

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

        EnchantmentTypeSqlModel that = (EnchantmentTypeSqlModel) o;

        return new EqualsBuilder()
            .append(this.getEnchantment(), that.getEnchantment())
            .append(this.getItemTypes(), that.getItemTypes())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getEnchantment())
            .append(this.getItemTypes())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
