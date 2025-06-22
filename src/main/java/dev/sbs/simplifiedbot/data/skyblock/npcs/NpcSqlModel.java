package dev.sbs.simplifiedbot.data.skyblock.npcs;

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
    name = "skyblock_npcs",
    indexes = {
        @Index(
            columnList = "x, y, z, key",
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
public class NpcSqlModel implements NpcModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "x")
    private Double x;

    @Setter
    @Column(name = "y")
    private Double y;

    @Setter
    @Column(name = "z")
    private Double z;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "location_key", referencedColumnName = "key")
    private LocationSqlModel location;

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

        NpcSqlModel that = (NpcSqlModel) o;

        return new EqualsBuilder()
            .append(this.getId(), that.getId())
            .append(this.getX(), that.getX())
            .append(this.getY(), that.getY())
            .append(this.getZ(), that.getZ())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
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
            .append(this.getKey())
            .append(this.getName())
            .append(this.getLocation())
            .append(this.getLocationArea())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
