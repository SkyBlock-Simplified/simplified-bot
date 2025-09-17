package dev.sbs.simplifiedbot.model.sql;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.sql.SqlModel;
import dev.sbs.api.data.sql.converter.ColorConverter;
import dev.sbs.simplifiedbot.model.AppGuildEmbed;
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
import java.awt.*;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "discord_guild_embeds",
    indexes = {
        @Index(
            columnList = "guild_id, key",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlAppGuildEmbed implements AppGuildEmbed, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "guild_id", referencedColumnName = "guild_id")
    private SqlAppGuild guild;

    @Setter
    @Column(name = "title", nullable = false)
    private String title;

    @Setter
    @Column(name = "color", nullable = false)
    @Convert(converter = ColorConverter.class)
    private Color color;

    @Setter
    @Column(name = "url")
    private String url;

    @Setter
    @Column(name = "description")
    private String description;

    @Setter
    @Column(name = "author_name")
    private String authorName;

    @Setter
    @Column(name = "author_url")
    private String authorUrl;

    @Setter
    @Column(name = "author_icon_url")
    private String authorIconUrl;

    @Setter
    @Column(name = "image_url")
    private String imageUrl;

    @Setter
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Setter
    @Column(name = "video_url")
    private String videoUrl;

    @Setter
    @Column(name = "timestamp")
    private Instant timestamp;

    @Setter
    @Column(name = "footer_text")
    private String footerText;

    @Setter
    @Column(name = "footer_icon_url")
    private String footerIconUrl;

    @Setter
    @Column(name = "notes")
    private String notes;

    @Setter
    @Column(name = "submitter_discord_id")
    private Long submitterDiscordId;

    @Setter
    @Column(name = "editor_discord_id")
    private Long editorDiscordId;

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

        SqlAppGuildEmbed that = (SqlAppGuildEmbed) o;

        return new EqualsBuilder()
            .append(this.getGuild(), that.getGuild())
            .append(this.getKey(), that.getKey())
            .append(this.getTitle(), that.getTitle())
            .append(this.getColor(), that.getColor())
            .append(this.getUrl(), that.getUrl())
            .append(this.getDescription(), that.getDescription())
            .append(this.getAuthorName(), that.getAuthorName())
            .append(this.getAuthorUrl(), that.getAuthorUrl())
            .append(this.getAuthorIconUrl(), that.getAuthorIconUrl())
            .append(this.getImageUrl(), that.getImageUrl())
            .append(this.getThumbnailUrl(), that.getThumbnailUrl())
            .append(this.getVideoUrl(), that.getVideoUrl())
            .append(this.getTimestamp(), that.getTimestamp())
            .append(this.getFooterText(), that.getFooterText())
            .append(this.getFooterIconUrl(), that.getFooterIconUrl())
            .append(this.getNotes(), that.getNotes())
            .append(this.getSubmitterDiscordId(), that.getSubmitterDiscordId())
            .append(this.getEditorDiscordId(), that.getEditorDiscordId())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getGuild())
            .append(this.getKey())
            .append(this.getTitle())
            .append(this.getColor())
            .append(this.getUrl())
            .append(this.getDescription())
            .append(this.getAuthorName())
            .append(this.getAuthorUrl())
            .append(this.getAuthorIconUrl())
            .append(this.getImageUrl())
            .append(this.getThumbnailUrl())
            .append(this.getVideoUrl())
            .append(this.getTimestamp())
            .append(this.getFooterText())
            .append(this.getFooterIconUrl())
            .append(this.getNotes())
            .append(this.getSubmitterDiscordId())
            .append(this.getEditorDiscordId())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
