package dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforge_conditions;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforges.ReforgeSqlModel;
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
    name = "skyblock_reforge_conditions",
    indexes = {
        @Index(
            columnList = "reforge_key, item_id",
            unique = true
        ),
        @Index(
            columnList = "item_id"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ReforgeConditionSqlModel implements ReforgeConditionModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "reforge_key", referencedColumnName = "key")
    private ReforgeSqlModel reforge;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private ItemSqlModel item;

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

        ReforgeConditionSqlModel that = (ReforgeConditionSqlModel) o;

        return new EqualsBuilder()
            .append(this.getReforge(), that.getReforge())
            .append(this.getItem(), that.getItem())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getReforge())
            .append(this.getItem())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
