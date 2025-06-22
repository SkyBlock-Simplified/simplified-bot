package dev.sbs.simplifiedbot.data.skyblock.bestiary_data.bestiary_brackets;

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
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "skyblock_bestiary_brackets",
    indexes = {
        @Index(
            columnList = "bracket, tier",
            unique = true
        ),
        @Index(
            columnList = "tier"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BestiaryBracketSqlModel implements BestiaryBracketModel, SqlModel {

    @Id
    @Setter
    @Column(name = "bracket")
    private Integer bracket;

    @Id
    @Setter
    @Column(name = "tier")
    private Integer tier;

    @Setter
    @Column(name = "total_kills_required", nullable = false)
    private Integer totalKillsRequired;

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

        BestiaryBracketSqlModel that = (BestiaryBracketSqlModel) o;

        return new EqualsBuilder()
            .append(this.getBracket(), that.getBracket())
            .append(this.getTier(), that.getTier())
            .append(this.getTotalKillsRequired(), that.getTotalKillsRequired())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getBracket())
            .append(this.getTier())
            .append(this.getTotalKillsRequired())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
