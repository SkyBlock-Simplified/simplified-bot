package dev.sbs.simplifiedbot.model.sql;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.sql.SqlModel;
import dev.sbs.simplifiedbot.model.AppGuildApplicationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "discord_guild_application_types",
    indexes = {
        @Index(
            columnList = "guild_id, key",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlAppGuildApplicationType implements AppGuildApplicationType, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Id
    @Setter
    @Column(name = "guild_id")
    private SqlAppGuild guild;

    @Setter
    @Column(name = "description")
    private String description;

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

        SqlAppGuildApplicationType that = (SqlAppGuildApplicationType) o;

        return new EqualsBuilder()
            .append(this.getGuild(), that.getGuild())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getDescription(), that.getDescription())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getGuild())
            .append(this.getKey())
            .append(this.getName())
            .append(this.getDescription())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
