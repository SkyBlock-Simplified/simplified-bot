package dev.sbs.simplifiedbot.data.skyblock.pet_data.pets;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_types.PetTypeSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RaritySqlModel;
import dev.sbs.simplifiedbot.data.skyblock.skills.SkillSqlModel;
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
    name = "skyblock_pets",
    indexes = {
        @Index(
            columnList = "lowest_rarity_key"
        ),
        @Index(
            columnList = "skill_key"
        ),
        @Index(
            columnList = "pet_type_key"
        ),
        @Index(
            columnList = "emoji_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PetSqlModel implements PetModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "lowest_rarity_key", referencedColumnName = "key")
    private RaritySqlModel lowestRarity;

    @Setter
    @ManyToOne
    @JoinColumn(name = "skill_key", referencedColumnName = "key")
    private SkillSqlModel skill;

    @Setter
    @ManyToOne
    @JoinColumn(name = "pet_type_key", referencedColumnName = "key")
    private PetTypeSqlModel petType;

    @Setter
    @Column(name = "skin", nullable = false)
    private String skin;

    @Setter
    @ManyToOne
    @JoinColumn(name = "emoji_key", referencedColumnName = "key")
    private EmojiSqlModel emoji;

    @Setter
    @Column(name = "max_level", nullable = false)
    private Integer maxLevel = 100;

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

        PetSqlModel that = (PetSqlModel) o;

        return new EqualsBuilder()
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getLowestRarity(), that.getLowestRarity())
            .append(this.getSkill(), that.getSkill())
            .append(this.getPetType(), that.getPetType())
            .append(this.getSkin(), that.getSkin())
            .append(this.getEmoji(), that.getEmoji())
            .append(this.getMaxLevel(), that.getMaxLevel())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getLowestRarity())
            .append(this.getSkill())
            .append(this.getPetType())
            .append(this.getSkin())
            .append(this.getEmoji())
            .append(this.getMaxLevel())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
