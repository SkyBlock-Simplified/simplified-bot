package dev.sbs.simplifiedbot.data.skyblock.trophy_fishes;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.location_data.location_areas.LocationAreaSqlModel;
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
    name = "skyblock_trophy_fishes",
    indexes = {
        @Index(
            columnList = "rarity_key"
        ),
        @Index(
            columnList = "location_area_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TrophyFishSqlModel implements TrophyFishModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "rarity_key", referencedColumnName = "key", nullable = false)
    private RaritySqlModel rarity;

    @Setter
    @ManyToOne
    @JoinColumn(name = "location_area_key", referencedColumnName = "key")
    private LocationAreaSqlModel locationArea;

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

        TrophyFishSqlModel that = (TrophyFishSqlModel) o;

        return new EqualsBuilder()
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getRarity(), that.getRarity())
            .append(this.getLocationArea(), that.getLocationArea())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getRarity())
            .append(this.getLocationArea())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
