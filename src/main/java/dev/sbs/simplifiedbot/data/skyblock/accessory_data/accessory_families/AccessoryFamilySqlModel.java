package dev.sbs.simplifiedbot.data.skyblock.accessory_data.accessory_families;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "skyblock_accessory_families"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AccessoryFamilySqlModel implements AccessoryFamilyModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "reforges_stackable")
    private boolean reforgesStackable;

    @Setter
    @Column(name = "stats_stackable")
    private boolean statsStackable;

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

        AccessoryFamilySqlModel that = (AccessoryFamilySqlModel) o;

        return new EqualsBuilder()
            .append(this.isReforgesStackable(), that.isReforgesStackable())
            .append(this.isStatsStackable(), that.isStatsStackable())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.isReforgesStackable())
            .append(this.isStatsStackable())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
