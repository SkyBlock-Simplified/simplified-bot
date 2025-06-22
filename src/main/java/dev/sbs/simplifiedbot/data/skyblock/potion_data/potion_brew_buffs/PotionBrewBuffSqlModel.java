package dev.sbs.simplifiedbot.data.skyblock.potion_data.potion_brew_buffs;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.potion_data.potion_brews.PotionBrewSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "skyblock_potion_brew_buffs",
    indexes = {
        @Index(
            columnList = "potion_brew_key, buff_key, amplified",
            unique = true
        ),
        @Index(
            columnList = "potion_brew_key, amplified"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PotionBrewBuffSqlModel implements PotionBrewBuffModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "potion_brew_key", nullable = false, referencedColumnName = "key"),
        @JoinColumn(name = "amplified", nullable = false, referencedColumnName = "amplified")
    })
    private PotionBrewSqlModel potionBrew;

    @Setter
    @Column(name = "buff_key", nullable = false)
    private String buffKey;

    @Setter
    @Column(name = "buff_value", nullable = false)
    private Double buffValue;

    @Setter
    @Column(name = "percentage", nullable = false)
    private boolean percentage;

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

        PotionBrewBuffSqlModel that = (PotionBrewBuffSqlModel) o;

        return new EqualsBuilder()
            .append(this.isPercentage(), that.isPercentage())
            .append(this.getId(), that.getId())
            .append(this.getPotionBrew(), that.getPotionBrew())
            .append(this.getBuffKey(), that.getBuffKey())
            .append(this.getBuffValue(), that.getBuffValue())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getPotionBrew())
            .append(this.getBuffKey())
            .append(this.getBuffValue())
            .append(this.isPercentage())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
