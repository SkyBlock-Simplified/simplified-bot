package dev.sbs.simplifiedbot.data.discord.guild_data.guild_tickets;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.discord.guild_data.guild_embeds.GuildEmbedSqlModel;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildSqlModel;
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
    name = "discord_guild_tickets",
    indexes = {
        @Index(
            columnList = "guild_id, key",
            unique = true
        ),
        @Index(
            columnList = "guild_id, embed_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GuildTicketSqlModel implements GuildTicketModel, SqlModel {

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
    @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", insertable = false, updatable = false)
    private GuildSqlModel guild;

    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = false),
        @JoinColumn(name = "embed_key", referencedColumnName = "key", nullable = false)
    })
    private GuildEmbedSqlModel embed;

    @Setter
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Setter
    @Column(name = "notes")
    private String notes;

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

        GuildTicketSqlModel that = (GuildTicketSqlModel) o;

        return new EqualsBuilder()
            .append(this.isEnabled(), that.isEnabled())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getGuild(), that.getGuild())
            .append(this.getEmbed(), that.getEmbed())
            .append(this.getNotes(), that.getNotes())
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
            .append(this.getEmbed())
            .append(this.isEnabled())
            .append(this.getNotes())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
