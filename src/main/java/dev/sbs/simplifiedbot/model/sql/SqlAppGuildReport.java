package dev.sbs.simplifiedbot.model.sql;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.sql.SqlModel;
import dev.sbs.api.data.sql.converter.list.StringListConverter;
import dev.sbs.simplifiedbot.model.AppGuildReport;
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
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;

@Getter
@Entity
@Table(
    name = "discord_guild_reports",
    indexes = {
        @Index(
            columnList = "guild_id, report_type_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlAppGuildReport implements AppGuildReport, SqlModel {

    @Id
    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "guild_id", nullable = false, referencedColumnName = "guild_id"),
        @JoinColumn(name = "report_type_key", nullable = false, referencedColumnName = "key")
    })
    private SqlAppGuildReportType type;

    @Setter
    @Column(name = "reported_discord_id")
    private Long reportedDiscordId;

    @Setter
    @Column(name = "reported_mojang_uuid")
    private String reportedMojangUniqueId;

    @Setter
    @Column(name = "submitter_discord_id", nullable = false)
    private Long submitterDiscordId;

    @Setter
    @Column(name = "assignee_discord_id")
    private Long assigneeDiscordId;

    @Setter
    @Column(name = "reason")
    private String reason;

    @Setter
    @Column(name = "proof")
    private String proof;

    @Setter
    @Column(name = "media_links", nullable = false)
    @Convert(converter = StringListConverter.class)
    private List<String> mediaLinks;

    @Setter
    @Column(name = "notes")
    private String notes;

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

        SqlAppGuildReport that = (SqlAppGuildReport) o;

        return new EqualsBuilder()
            .append(this.getType(), that.getType())
            .append(this.getReportedDiscordId(), that.getReportedDiscordId())
            .append(this.getReportedMojangUniqueId(), that.getReportedMojangUniqueId())
            .append(this.getSubmitterDiscordId(), that.getSubmitterDiscordId())
            .append(this.getAssigneeDiscordId(), that.getAssigneeDiscordId())
            .append(this.getReason(), that.getReason())
            .append(this.getProof(), that.getProof())
            .append(this.getMediaLinks(), that.getMediaLinks())
            .append(this.getNotes(), that.getNotes())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getType())
            .append(this.getReportedDiscordId())
            .append(this.getReportedMojangUniqueId())
            .append(this.getSubmitterDiscordId())
            .append(this.getAssigneeDiscordId())
            .append(this.getReason())
            .append(this.getProof())
            .append(this.getMediaLinks())
            .append(this.getNotes())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
