package dev.sbs.simplifiedbot.data.discord.sbs_data.sbs_legacy_donors;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
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
    name = "discord_sbs_legacy_donors"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SbsLegacyDonorSqlModel implements SbsLegacyDonorModel, SqlModel {

    @Id
    @Setter
    @Column(name = "discord_id", nullable = false, unique = true)
    private Long discordId;

    @Setter
    @Column(name = "amount", nullable = false)
    private Double amount;

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

        SbsLegacyDonorSqlModel that = (SbsLegacyDonorSqlModel) o;

        return new EqualsBuilder()
            .append(this.getDiscordId(), that.getDiscordId())
            .append(this.getAmount(), that.getAmount())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getDiscordId())
            .append(this.getAmount())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
