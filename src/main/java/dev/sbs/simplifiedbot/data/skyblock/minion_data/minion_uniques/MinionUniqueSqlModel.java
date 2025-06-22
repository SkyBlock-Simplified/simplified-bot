package dev.sbs.simplifiedbot.data.skyblock.minion_data.minion_uniques;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "skyblock_minion_uniques"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MinionUniqueSqlModel implements MinionUniqueModel, SqlModel {

    @Id
    @Setter
    @Column(name = "placeable", nullable = false, unique = true)
    private Integer placeable;

    @Setter
    @Column(name = "unique_crafts", nullable = false)
    private Integer uniqueCrafts;

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

        MinionUniqueSqlModel that = (MinionUniqueSqlModel) o;

        return new EqualsBuilder()
            .append(this.getPlaceable(), that.getPlaceable())
            .append(this.getUniqueCrafts(), that.getUniqueCrafts())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getPlaceable())
            .append(this.getUniqueCrafts())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
