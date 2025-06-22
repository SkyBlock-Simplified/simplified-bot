package dev.sbs.simplifiedbot.data.discord.guild_data.guild_skyblock_events;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.list.LongListConverter;
import dev.sbs.simplifiedbot.data.discord.guild_data.guilds.GuildSqlModel;
import dev.sbs.simplifiedbot.data.discord.skyblock_events.SkyBlockEventSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;

@Getter
@Entity
@Table(
    name = "discord_guild_skyblock_events",
    indexes = {
        @Index(
            columnList = "guild_id, event_key",
            unique = true
        ),
        @Index(
            columnList = "guild_id"
        ),
        @Index(
            columnList = "event_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GuildSkyBlockEventSqlModel implements GuildSkyBlockEventModel, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "guild_id", referencedColumnName = "guild_id")
    private GuildSqlModel guild;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "event_key", referencedColumnName = "key")
    private SkyBlockEventSqlModel event;

    @Setter
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Setter
    @Column(name = "mention_roles", nullable = false)
    @Convert(converter = LongListConverter.class)
    private List<Long> mentionRoles;

    @Setter
    @Column(name = "webhook_url")
    private String webhookUrl;

    @Setter
    @Column(name = "channel_id")
    private Long channelId;

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

        GuildSkyBlockEventSqlModel that = (GuildSkyBlockEventSqlModel) o;

        return new EqualsBuilder()
            .append(this.isEnabled(), that.isEnabled())
            .append(this.getGuild(), that.getGuild())
            .append(this.getEvent(), that.getEvent())
            .append(this.getMentionRoles(), that.getMentionRoles())
            .append(this.getWebhookUrl(), that.getWebhookUrl())
            .append(this.getChannelId(), that.getChannelId())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getGuild())
            .append(this.getEvent())
            .append(this.isEnabled())
            .append(this.getMentionRoles())
            .append(this.getWebhookUrl())
            .append(this.getChannelId())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
