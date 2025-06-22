package dev.sbs.simplifiedbot.data.skyblock.skills;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemSqlModel;
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

@Getter
@Entity
@Table(
    name = "skyblock_skills",
    indexes = {
        @Index(
            columnList = "item_id"
        ),
        @Index(
            columnList = "emoji_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SkillSqlModel implements SkillModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "description", nullable = false)
    private String description;

    @Setter
    @Column(name = "max_level", nullable = false)
    private Integer maxLevel;

    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private ItemSqlModel item;

    @Setter
    @ManyToOne
    @JoinColumn(name = "emoji_key", referencedColumnName = "key")
    private EmojiSqlModel emoji;

    @Setter
    @Column(name = "cosmetic", nullable = false)
    private boolean cosmetic;

    @Setter
    @Column(name = "weight_exponent", nullable = false)
    private Double weightExponent;

    @Setter
    @Column(name = "weight_divider", nullable = false)
    private Double weightDivider;

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

        SkillSqlModel that = (SkillSqlModel) o;

        return new EqualsBuilder()
            .append(this.isCosmetic(), that.isCosmetic())
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getDescription(), that.getDescription())
            .append(this.getMaxLevel(), that.getMaxLevel())
            .append(this.getItem(), that.getItem())
            .append(this.getEmoji(), that.getEmoji())
            .append(this.getWeightExponent(), that.getWeightExponent())
            .append(this.getWeightDivider(), that.getWeightDivider())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getDescription())
            .append(this.getMaxLevel())
            .append(this.getItem())
            .append(this.getEmoji())
            .append(this.isCosmetic())
            .append(this.getWeightExponent())
            .append(this.getWeightDivider())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
