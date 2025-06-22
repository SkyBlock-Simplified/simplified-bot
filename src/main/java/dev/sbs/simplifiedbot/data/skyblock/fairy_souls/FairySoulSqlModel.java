package dev.sbs.simplifiedbot.data.skyblock.fairy_souls;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.location_data.location_areas.LocationAreaSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.location_data.locations.LocationSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "skyblock_fairy_souls",
    indexes = {
        @Index(
            columnList = "x, y, z, location_key",
            unique = true
        ),
        @Index(
            columnList = "location_key"
        ),
        @Index(
            columnList = "location_area_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FairySoulSqlModel implements FairySoulModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "x", nullable = false)
    private Double x;

    @Setter
    @Column(name = "y", nullable = false)
    private Double y;

    @Setter
    @Column(name = "z", nullable = false)
    private Double z;

    @Setter
    @ManyToOne
    @JoinColumn(name = "location_key", referencedColumnName = "key")
    private LocationSqlModel location;

    @Setter
    @ManyToOne
    @JoinColumn(name = "location_area_key", referencedColumnName = "key")
    private LocationAreaSqlModel locationArea;

    @Setter
    @Column(name = "walkable", nullable = false)
    private boolean walkable;

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

        FairySoulSqlModel that = (FairySoulSqlModel) o;

        return new EqualsBuilder()
            .append(this.isWalkable(), that.isWalkable())
            .append(this.getId(), that.getId())
            .append(this.getX(), that.getX())
            .append(this.getY(), that.getY())
            .append(this.getZ(), that.getZ())
            .append(this.getLocation(), that.getLocation())
            .append(this.getLocationArea(), that.getLocationArea())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getX())
            .append(this.getY())
            .append(this.getZ())
            .append(this.getLocation())
            .append(this.getLocationArea())
            .append(this.isWalkable())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
