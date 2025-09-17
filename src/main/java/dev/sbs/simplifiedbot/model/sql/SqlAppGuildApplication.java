package dev.sbs.simplifiedbot.model.sql;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.sql.SqlModel;
import dev.sbs.simplifiedbot.model.AppGuildApplication;
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
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "discord_guild_applications",
    indexes = {
        @Index(
            columnList = "guild_id, key",
            unique = true
        ),
        @Index(
            columnList = "guild_id, type_key"
        ),
        @Index(
            columnList = "guild_id, embed_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlAppGuildApplication implements AppGuildApplication, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = false)
    private SqlAppGuild guild;

    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = false, updatable = false, insertable = false),
        @JoinColumn(name = "type_key", referencedColumnName = "key", nullable = false, updatable = false, insertable = false)
    })
    private SqlAppGuildApplicationType type;

    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = false, updatable = false, insertable = false),
        @JoinColumn(name = "embed_key", referencedColumnName = "key", nullable = false, updatable = false, insertable = false)
    })
    private SqlAppGuildEmbed embed;

    @Setter
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Setter
    @Column(name = "notes")
    private String notes;

    @Setter
    @Column(name = "live_at")
    private Instant liveAt;

    @Setter
    @Column(name = "close_at")
    private Instant closeAt;

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

        SqlAppGuildApplication that = (SqlAppGuildApplication) o;

        return new EqualsBuilder()
            .append(this.isEnabled(), that.isEnabled())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getGuild(), that.getGuild())
            .append(this.getType(), that.getType())
            .append(this.getEmbed(), that.getEmbed())
            .append(this.getNotes(), that.getNotes())
            .append(this.getLiveAt(), that.getLiveAt())
            .append(this.getCloseAt(), that.getCloseAt())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getGuild())
            .append(this.getType())
            .append(this.getEmbed())
            .append(this.isEnabled())
            .append(this.getNotes())
            .append(this.getLiveAt())
            .append(this.getCloseAt())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
