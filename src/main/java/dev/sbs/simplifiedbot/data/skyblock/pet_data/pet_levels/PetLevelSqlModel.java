package dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_levels;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RaritySqlModel;
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
    name = "skyblock_pet_levels",
    indexes = {
        @Index(
            columnList = "rarity_key"
        ),
        @Index(
            columnList = "rarity_key, level",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PetLevelSqlModel implements PetLevelModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "rarity_key", referencedColumnName = "key")
    private RaritySqlModel rarity;

    @Id
    @Setter
    @Column(name = "level")
    private Integer level;

    @Setter
    @Column(name = "value", nullable = false)
    private Double value;

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

        PetLevelSqlModel that = (PetLevelSqlModel) o;

        return new EqualsBuilder()
            .append(this.getRarity(), that.getRarity())
            .append(this.getLevel(), that.getLevel())
            .append(this.getValue(), that.getValue())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getRarity())
            .append(this.getLevel())
            .append(this.getValue())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
