package dev.sbs.simplifiedbot.data.discord.skyblock_event_timers;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.discord.skyblock_events.SkyBlockEventSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.seasons.SeasonSqlModel;
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
    name = "discord_skyblock_event_timers",
    indexes = {
        @Index(
            columnList = "event_key"
        ),
        @Index(
            columnList = "start_season_key"
        ),
        @Index(
            columnList = "end_season_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SkyBlockEventTimerSqlModel implements SkyBlockEventTimerModel, SqlModel {

    @Id
    @Setter
    @Column(name = "event_key")
    private SkyBlockEventSqlModel event;

    @Setter
    @ManyToOne
    @JoinColumn(name = "start_season_key", referencedColumnName = "key", nullable = false)
    private SeasonSqlModel start;

    @Setter
    @Column(name = "start_season_day", nullable = false)
    private Integer startDay;

    @Setter
    @ManyToOne
    @JoinColumn(name = "end_season_key", referencedColumnName = "key", nullable = false)
    private SeasonSqlModel end;

    @Setter
    @Column(name = "end_season_day", nullable = false)
    private Integer endDay;

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

        SkyBlockEventTimerSqlModel that = (SkyBlockEventTimerSqlModel) o;

        return new EqualsBuilder()
            .append(this.getEvent(), that.getEvent())
            .append(this.getStart(), that.getStart())
            .append(this.getStartDay(), that.getStartDay())
            .append(this.getEnd(), that.getEnd())
            .append(this.getEndDay(), that.getEndDay())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getEvent())
            .append(this.getStart())
            .append(this.getStartDay())
            .append(this.getEnd())
            .append(this.getEndDay())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
