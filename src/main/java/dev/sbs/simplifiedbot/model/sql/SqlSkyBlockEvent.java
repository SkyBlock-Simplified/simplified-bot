package dev.sbs.simplifiedbot.model.sql;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.sql.SqlModel;
import dev.sbs.simplifiedbot.model.SkyBlockEvent;
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
    name = "discord_skyblock_events",
    indexes = {
        @Index(
            columnList = "emoji_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlSkyBlockEvent implements SkyBlockEvent, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "emoji_key", referencedColumnName = "key")
    private SqlAppEmoji botEmoji;

    @Setter
    @Column(name = "description", nullable = false)
    private String description;

    @Setter
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Setter
    @Column(name = "status")
    private String status;

    @Setter
    @Column(name = "interval_expression")
    private String intervalExpression;

    @Setter
    @Column(name = "thirdparty_json_url")
    private String thirdPartyJsonUrl;

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

        SqlSkyBlockEvent that = (SqlSkyBlockEvent) o;

        return new EqualsBuilder()
            .append(this.isEnabled(), that.isEnabled())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getBotEmoji(), that.getBotEmoji())
            .append(this.getDescription(), that.getDescription())
            .append(this.getStatus(), that.getStatus())
            .append(this.getIntervalExpression(), that.getIntervalExpression())
            .append(this.getThirdPartyJsonUrl(), that.getThirdPartyJsonUrl())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getBotEmoji())
            .append(this.getDescription())
            .append(this.isEnabled())
            .append(this.getStatus())
            .append(this.getIntervalExpression())
            .append(this.getThirdPartyJsonUrl())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
