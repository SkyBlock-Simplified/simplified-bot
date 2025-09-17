package dev.sbs.simplifiedbot.model.sql;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.sql.SqlModel;
import dev.sbs.simplifiedbot.model.SbsBetaTester;
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
    name = "discord_sbs_beta_testers",
    indexes = {
        @Index(
            columnList = "discord_id, early",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlSbsBetaTester implements SbsBetaTester, SqlModel {

    @Id
    @Setter
    @Column(name = "discord_id")
    private Long discordId;

    @Id
    @Setter
    @Column(name = "early")
    private boolean early;

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

        SqlSbsBetaTester that = (SqlSbsBetaTester) o;

        return new EqualsBuilder()
            .append(this.isEarly(), that.isEarly())
            .append(this.getDiscordId(), that.getDiscordId())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getDiscordId())
            .append(this.isEarly())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
