package dev.sbs.simplifiedbot.data.discord.emojis;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "discord_emojis",
    indexes = {
        @Index(
            columnList = "guild_id"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EmojiSqlModel implements EmojiModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "emoji_id", nullable = false, unique = true)
    private Long emojiId;

    @Setter
    @ManyToOne
    @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = false)
    private GuildSqlModel guild;

    @Setter
    @Column(name = "animated", nullable = false)
    private boolean animated;

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

        EmojiSqlModel that = (EmojiSqlModel) o;

        return new EqualsBuilder()
            .append(this.isAnimated(), that.isAnimated())
            .append(this.getEmojiId(), that.getEmojiId())
            .append(this.getGuild(), that.getGuild())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getEmojiId())
            .append(this.getGuild())
            .append(this.getKey())
            .append(this.getName())
            .append(this.isAnimated())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
