package dev.sbs.simplifiedbot.model.sql;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.sql.SqlModel;
import dev.sbs.simplifiedbot.model.AppGuildReputation;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "discord_guild_reputation",
    indexes = {
        @Index(
            columnList = "guild_id, reputation_type_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlAppGuildReputation implements AppGuildReputation, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "guild_id", nullable = false, referencedColumnName = "guild_id"),
        @JoinColumn(name = "reputation_type_key", nullable = false, referencedColumnName = "key")
    })
    private SqlAppGuildReputationType type;

    @Setter
    @Column(name = "receiver_discord_id", nullable = false)
    private Long receiverDiscordId;

    @Setter
    @Column(name = "submitter_discord_id", nullable = false)
    private Long submitterDiscordId;

    @Setter
    @Column(name = "assignee_discord_id")
    private Long assigneeDiscordId;

    @Setter
    @Column(name = "reason")
    private String reason;

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

        SqlAppGuildReputation that = (SqlAppGuildReputation) o;

        return new EqualsBuilder()
            .append(this.getId(), that.getId())
            .append(this.getType(), that.getType())
            .append(this.getReceiverDiscordId(), that.getReceiverDiscordId())
            .append(this.getSubmitterDiscordId(), that.getSubmitterDiscordId())
            .append(this.getAssigneeDiscordId(), that.getAssigneeDiscordId())
            .append(this.getReason(), that.getReason())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getType())
            .append(this.getReceiverDiscordId())
            .append(this.getSubmitterDiscordId())
            .append(this.getAssigneeDiscordId())
            .append(this.getReason())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
