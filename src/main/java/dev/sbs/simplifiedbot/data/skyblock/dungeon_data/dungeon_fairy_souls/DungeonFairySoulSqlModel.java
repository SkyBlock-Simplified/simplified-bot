package dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeon_fairy_souls;

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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "skyblock_dungeon_fairy_souls"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DungeonFairySoulSqlModel implements DungeonFairySoulModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "room", nullable = false)
    private String room;

    @Setter
    @Column(name = "description", nullable = false)
    private String description;

    @Setter
    @Column(name = "where", nullable = false)
    private String where;

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

        DungeonFairySoulSqlModel that = (DungeonFairySoulSqlModel) o;

        return new EqualsBuilder()
            .append(this.isWalkable(), that.isWalkable())
            .append(this.getId(), that.getId())
            .append(this.getRoom(), that.getRoom())
            .append(this.getDescription(), that.getDescription())
            .append(this.getWhere(), that.getWhere())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getRoom())
            .append(this.getDescription())
            .append(this.getWhere())
            .append(this.isWalkable())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
