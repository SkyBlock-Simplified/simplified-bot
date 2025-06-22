package dev.sbs.simplifiedbot.data.discord.guild_data.guilds;

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
    name = "discord_guilds"
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GuildSqlModel implements GuildModel, SqlModel {

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

        GuildSqlModel that = (GuildSqlModel) o;

        return new EqualsBuilder()
            .append(this.isReportsPublic(), that.isReportsPublic())
            .append(this.isEmojiServer(), that.isEmojiServer())
            .append(this.getGuildId(), that.getGuildId())
            .append(this.getName(), that.getName())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getGuildId())
            .append(this.getName())
            .append(this.isReportsPublic())
            .append(this.isEmojiServer())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
