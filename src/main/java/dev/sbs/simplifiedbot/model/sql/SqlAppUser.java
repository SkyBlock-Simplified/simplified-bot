package dev.sbs.simplifiedbot.model.sql;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.sql.SqlModel;
import dev.sbs.api.data.sql.converter.list.LongListConverter;
import dev.sbs.api.data.sql.converter.list.UUIDListConverter;
import dev.sbs.api.data.sql.converter.map.LongStringMapConverter;
import dev.sbs.simplifiedbot.model.AppUser;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Entity
@Table(
    name = "discord_users"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlAppUser implements AppUser, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "discord_ids", nullable = false)
    @Convert(converter = LongListConverter.class)
    private List<Long> discordIds;

    @Setter
    @Column(name = "mojang_uuids", nullable = false)
    @Convert(converter = UUIDListConverter.class)
    private List<UUID> mojangUniqueIds;

    @Setter
    @Column(name = "notes", nullable = false)
    @Convert(converter = LongStringMapConverter.class)
    private Map<Long, String> notes;

    @Setter
    @Column(name = "guild_interaction_blacklisted", nullable = false)
    @Convert(converter = LongListConverter.class)
    private List<Long> guildInteractionBlacklisted;

    @Setter
    @Column(name = "is_developer", nullable = false)
    private boolean developer;

    @Setter
    @Column(name = "developer_protected", nullable = false)
    private boolean developerProtected;

    @Setter
    @Column(name = "developer_interaction_enabled", nullable = false)
    private boolean botInteractionEnabled;

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

        SqlAppUser that = (SqlAppUser) o;

        return new EqualsBuilder()
            .append(this.isDeveloper(), that.isDeveloper())
            .append(this.isDeveloperProtected(), that.isDeveloperProtected())
            .append(this.isBotInteractionEnabled(), that.isBotInteractionEnabled())
            .append(this.getId(), that.getId())
            .append(this.getDiscordIds(), that.getDiscordIds())
            .append(this.getMojangUniqueIds(), that.getMojangUniqueIds())
            .append(this.getNotes(), that.getNotes())
            .append(this.getGuildInteractionBlacklisted(), that.getGuildInteractionBlacklisted())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getDiscordIds())
            .append(this.getMojangUniqueIds())
            .append(this.getNotes())
            .append(this.getGuildInteractionBlacklisted())
            .append(this.isDeveloper())
            .append(this.isDeveloperProtected())
            .append(this.isBotInteractionEnabled())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
