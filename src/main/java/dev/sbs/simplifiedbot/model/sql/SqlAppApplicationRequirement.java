package dev.sbs.simplifiedbot.model.sql;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.sql.SqlModel;
import dev.sbs.simplifiedbot.model.AppApplicationRequirement;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "discord_application_requirements"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlAppApplicationRequirement implements AppApplicationRequirement, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "description", nullable = false)
    private String description;

    @Setter
    @Column(name = "ordinal", nullable = false, unique = true)
    private Integer ordinal;

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

        SqlAppApplicationRequirement that = (SqlAppApplicationRequirement) o;

        return new EqualsBuilder()
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getDescription(), that.getDescription())
            .append(this.getOrdinal(), that.getOrdinal())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getDescription())
            .append(this.getOrdinal())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
