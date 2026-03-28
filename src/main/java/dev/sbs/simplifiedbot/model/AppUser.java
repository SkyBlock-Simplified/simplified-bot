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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(
    name = "discord_users"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AppUser implements JpaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "discord_ids", nullable = false)
    private List<Long> discordIds;

    @Setter
    @Column(name = "mojang_uuids", nullable = false)
    private List<UUID> mojangUniqueIds;

    @Setter
    @Column(name = "notes", nullable = false)
    private Map<Long, String> notes;

    @Setter
    @Column(name = "guild_interaction_blacklisted", nullable = false)
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

        AppUser that = (AppUser) o;

        return this.isDeveloper() == that.isDeveloper()
            && this.isDeveloperProtected() == that.isDeveloperProtected()
            && this.isBotInteractionEnabled() == that.isBotInteractionEnabled()
            && Objects.equals(this.getId(), that.getId())
            && Objects.equals(this.getDiscordIds(), that.getDiscordIds())
            && Objects.equals(this.getMojangUniqueIds(), that.getMojangUniqueIds())
            && Objects.equals(this.getNotes(), that.getNotes())
            && Objects.equals(this.getGuildInteractionBlacklisted(), that.getGuildInteractionBlacklisted())
            && Objects.equals(this.getUpdatedAt(), that.getUpdatedAt())
            && Objects.equals(this.getSubmittedAt(), that.getSubmittedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getDiscordIds(), this.getMojangUniqueIds(), this.getNotes(), this.getGuildInteractionBlacklisted(), this.isDeveloper(), this.isDeveloperProtected(), this.isBotInteractionEnabled(), this.getUpdatedAt(), this.getSubmittedAt());
    }

}