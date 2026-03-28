package dev.sbs.simplifiedbot.model;

import dev.sbs.api.persistence.JpaModel;
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
import java.util.Objects;

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
public class SbsBetaTester implements JpaModel {

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

        SbsBetaTester that = (SbsBetaTester) o;

        return this.isEarly() == that.isEarly()
            && Objects.equals(this.getDiscordId(), that.getDiscordId())
            && Objects.equals(this.getUpdatedAt(), that.getUpdatedAt())
            && Objects.equals(this.getSubmittedAt(), that.getSubmittedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getDiscordId(), this.isEarly(), this.getUpdatedAt(), this.getSubmittedAt());
    }

}