package dev.sbs.simplifiedbot.data.skyblock.hot_potato_stats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.CacheExpiry;
import dev.sbs.api.data.sql.converter.list.StringListConverter;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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
import java.util.concurrent.TimeUnit;

@Getter
@Entity
@Table(
    name = "skyblock_hot_potato_stats",
    indexes = {
        @Index(
            columnList = "group_key, stat_key",
            unique = true
        ),
        @Index(
            columnList = "group_key"
        )
    }
)
@CacheExpiry(length = TimeUnit.HOURS, value = 24)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HotPotatoStatSqlModel implements HotPotatoStatModel, SqlModel {

    @Id
    @Setter
    @Column(name = "group_key")
    private String groupKey;

    @Id
    @Setter
    @ManyToOne
    @JoinColumn(name = "stat_key", referencedColumnName = "key")
    private StatSqlModel stat;

    @Setter
    @Convert(converter = StringListConverter.class)
    @Column(name = "item_types", nullable = false)
    private List<String> itemTypes;

    @Setter
    @Column(name = "value", nullable = false)
    private Integer value;

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

        HotPotatoStatSqlModel that = (HotPotatoStatSqlModel) o;

        return new EqualsBuilder()
            .append(this.getGroupKey(), that.getGroupKey())
            .append(this.getItemTypes(), that.getItemTypes())
            .append(this.getStat(), that.getStat())
            .append(this.getValue(), that.getValue())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getGroupKey())
            .append(this.getItemTypes())
            .append(this.getStat())
            .append(this.getValue())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
