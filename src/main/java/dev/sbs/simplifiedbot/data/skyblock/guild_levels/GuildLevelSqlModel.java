package dev.sbs.simplifiedbot.data.skyblock.guild_levels;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.CacheExpiry;
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
import java.util.concurrent.TimeUnit;

@Getter
@Entity
@Table(
    name = "skyblock_guild_levels"
)
@CacheExpiry(length = TimeUnit.HOURS, value = 24)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GuildLevelSqlModel implements GuildLevelModel, SqlModel {

    @Id
    @Setter
    @Column(name = "level", nullable = false, unique = true)
    private Integer level;

    @Setter
    @Column(name = "total_exp_required", nullable = false)
    private Double totalExpRequired;

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

        GuildLevelSqlModel that = (GuildLevelSqlModel) o;

        return new EqualsBuilder()
            .append(this.getLevel(), that.getLevel())
            .append(this.getTotalExpRequired(), that.getTotalExpRequired())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getLevel())
            .append(this.getTotalExpRequired())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
