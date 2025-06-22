package dev.sbs.simplifiedbot.data.skyblock.dungeon_data.dungeons;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Getter
@Entity
@Table(
    name = "skyblock_dungeons",
    indexes = {
        @Index(
            columnList = "emoji_key"
        )
    }
)
@CacheExpiry(length = TimeUnit.HOURS, value = 24)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DungeonSqlModel implements DungeonModel, SqlModel {

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
    private EmojiSqlModel emoji;

    @Setter
    @Column(name = "weight_multiplier", nullable = false)
    private Double weightMultiplier;

    @Setter
    @Column(name = "master_enabled", nullable = false)
    private boolean masterModeEnabled;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @UpdateTimestamp
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

}
