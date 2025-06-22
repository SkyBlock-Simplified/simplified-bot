package dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_application_entries;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_applications.GuildApplicationSqlModel;
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
    name = "discord_guild_application_entries",
    indexes = {
        @Index(
            columnList = "guild_id, application_key, submitter_discord_id",
            unique = true
        ),
        @Index(
            columnList = "guild_id, application_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GuildApplicationEntrySqlModel implements GuildApplicationEntryModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = false),
        @JoinColumn(name = "application_key", referencedColumnName = "key", nullable = false)
    })
    private GuildApplicationSqlModel application;

    @Setter
    @Column(name = "submitter_discord_id")
    private Long submitterDiscordId;

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

        GuildApplicationEntrySqlModel that = (GuildApplicationEntrySqlModel) o;

        return new EqualsBuilder()
            .append(this.getId(), that.getId())
            .append(this.getApplication(), that.getApplication())
            .append(this.getSubmitterDiscordId(), that.getSubmitterDiscordId())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getApplication())
            .append(this.getSubmitterDiscordId())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
