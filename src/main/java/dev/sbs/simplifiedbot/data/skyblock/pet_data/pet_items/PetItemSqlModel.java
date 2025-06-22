package dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_items;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.map.StringDoubleMapConverter;
import dev.sbs.api.data.sql.converter.map.StringObjectMapConverter;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Map;

@Getter
@Entity
@Table(
    name = "skyblock_pet_items"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PetItemSqlModel implements PetItemModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", nullable = false, unique = true)
    private ItemSqlModel item;

    @Setter
    @Column(name = "description", nullable = false)
    private String description;

    @Setter
    @Column(name = "percentage", nullable = false)
    private boolean percentage;

    @Setter
    @Column(name = "effects", nullable = false)
    @Convert(converter = StringDoubleMapConverter.class)
    private Map<String, Double> effects;

    @Setter
    @Column(name = "buff_effects", nullable = false)
    @Convert(converter = StringObjectMapConverter.class)
    private Map<String, Object> buffEffects;

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

        PetItemSqlModel that = (PetItemSqlModel) o;

        return new EqualsBuilder()
            .append(this.isPercentage(), that.isPercentage())
            .append(this.getItem(), that.getItem())
            .append(this.getDescription(), that.getDescription())
            .append(this.getEffects(), that.getEffects())
            .append(this.getBuffEffects(), that.getBuffEffects())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getItem())
            .append(this.getDescription())
            .append(this.isPercentage())
            .append(this.getEffects())
            .append(this.getBuffEffects())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
