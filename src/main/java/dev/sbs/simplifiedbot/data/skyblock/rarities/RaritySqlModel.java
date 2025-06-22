package dev.sbs.simplifiedbot.data.skyblock.rarities;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.CacheExpiry;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Getter
@Entity
@Table(
    name = "skyblock_rarities",
    indexes = {
        @Index(
            columnList = "emoji_key"
        )
    }
)
@CacheExpiry(length = TimeUnit.HOURS, value = 24)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RaritySqlModel implements RarityModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;

    @Setter
    @Column(name = "enrichable", nullable = false)
    private boolean enrichable;

    @Setter
    @Column(name = "mp_multiplier")
    private Integer magicPowerMultiplier;

    @Setter
    @Column(name = "emoji_key")
    private EmojiSqlModel emoji;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @UpdateTimestamp
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RaritySqlModel that = (RaritySqlModel) o;

        return new EqualsBuilder()
            .append(this.isEnrichable(), that.isEnrichable())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getOrdinal(), that.getOrdinal())
            .append(this.getMagicPowerMultiplier(), that.getMagicPowerMultiplier())
            .append(this.getEmoji(), that.getEmoji())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getOrdinal())
            .append(this.isEnrichable())
            .append(this.getMagicPowerMultiplier())
            .append(this.getEmoji())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
