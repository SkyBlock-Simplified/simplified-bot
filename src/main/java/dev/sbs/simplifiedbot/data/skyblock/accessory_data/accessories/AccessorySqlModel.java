package dev.sbs.simplifiedbot.data.skyblock.accessory_data.accessories;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import dev.sbs.simplifiedbot.data.skyblock.accessory_data.accessory_families.AccessoryFamilySqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RaritySqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
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
import java.util.Map;

@Getter
@Entity
@Table(
    name = "skyblock_accessories",
    indexes = {
        @Index(
            columnList = "family_key"
        ),
        @Index(
            columnList = "rarity_key"
        ),
        @Index(
            columnList = "family_key, family_rank",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AccessorySqlModel implements AccessoryModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private ItemSqlModel item;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "rarity_key", referencedColumnName = "key", nullable = false)
    private RaritySqlModel rarity;

    @Setter
    @ManyToOne
    @JoinColumn(name = "family_key", referencedColumnName = "key")
    private AccessoryFamilySqlModel family;

    @Setter
    @Column(name = "family_rank")
    private Integer familyRank;

    @Setter
    @Column(name = "effects", nullable = false)
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> effects;

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

        AccessorySqlModel that = (AccessorySqlModel) o;

        return new EqualsBuilder()
            .append(this.getItem(), that.getItem())
            .append(this.getName(), that.getName())
            .append(this.getRarity(), that.getRarity())
            .append(this.getFamily(), that.getFamily())
            .append(this.getFamilyRank(), that.getFamilyRank())
            .append(this.getEffects(), that.getEffects())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getItem())
            .append(this.getName())
            .append(this.getRarity())
            .append(this.getFamily())
            .append(this.getFamilyRank())
            .append(this.getEffects())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
