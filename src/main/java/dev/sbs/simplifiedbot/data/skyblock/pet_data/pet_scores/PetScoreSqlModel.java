package dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_scores;

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
    name = "skyblock_pet_scores"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PetScoreSqlModel implements PetScoreModel, SqlModel {

    @Id
    @Setter
    @Column(name = "breakpoint", nullable = false, unique = true)
    private Integer breakpoint;

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

        PetScoreSqlModel that = (PetScoreSqlModel) o;

        return new EqualsBuilder()
            .append(this.getBreakpoint(), that.getBreakpoint())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getBreakpoint())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
