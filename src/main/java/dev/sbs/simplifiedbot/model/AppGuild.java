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
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Getter
@Entity
@Table(
    name = "discord_guilds"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AppGuild implements JpaModel {

    @Id
    @Setter
    @Column(name = "guild_id")
    private Long guildId;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "reports_public", nullable = false)
    private boolean reportsPublic;

    @Setter
    @Column(name = "emoji_management", nullable = false)
    private boolean emojiServer;

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

        AppGuild that = (AppGuild) o;

        return this.isReportsPublic() == that.isReportsPublic()
            && this.isEmojiServer() == that.isEmojiServer()
            && Objects.equals(this.getGuildId(), that.getGuildId())
            && Objects.equals(this.getName(), that.getName())
            && Objects.equals(this.getUpdatedAt(), that.getUpdatedAt())
            && Objects.equals(this.getSubmittedAt(), that.getSubmittedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getGuildId(), this.getName(), this.isReportsPublic(), this.isEmojiServer(), this.getUpdatedAt(), this.getSubmittedAt());
    }

}