package dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_application_requirements;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.discord.application_requirements.ApplicationRequirementSqlModel;
import dev.sbs.simplifiedbot.data.discord.guild_data.application_data.guild_applications.GuildApplicationSqlModel;
import dev.sbs.simplifiedbot.data.discord.setting_types.SettingTypeSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Entity
@Table(
    name = "discord_guild_application_requirements",
    indexes = {
        @Index(
            columnList = "guild_id, application_key, requirement_key",
            unique = true
        ),
        @Index(
            columnList = "requirement_key"
        ),
        @Index(
            columnList = "setting_type_key"
        ),
        @Index(
            columnList = "application_key, guild_id"
        ),
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GuildApplicationRequirementSqlModel implements GuildApplicationRequirementModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = false),
        @JoinColumn(name = "application_key", referencedColumnName = "key", nullable = false)
    })
    private GuildApplicationSqlModel application;

    @Setter
    @ManyToOne
    @JoinColumn(name = "requirement_key", referencedColumnName = "key")
    private ApplicationRequirementSqlModel requirement;

    @Setter
    @ManyToOne
    @JoinColumn(name = "setting_type_key", referencedColumnName = "key")
    private SettingTypeSqlModel type;

    @Setter
    @Column(name = "value", nullable = false)
    private String value;

    @Setter
    @Column(name = "description")
    private String description;

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

        GuildApplicationRequirementSqlModel that = (GuildApplicationRequirementSqlModel) o;

        return new EqualsBuilder()
            .append(this.getId(), that.getId())
            .append(this.getApplication(), that.getApplication())
            .append(this.getRequirement(), that.getRequirement())
            .append(this.getType(), that.getType())
            .append(this.getValue(), that.getValue())
            .append(this.getDescription(), that.getDescription())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getId())
            .append(this.getApplication())
            .append(this.getRequirement())
            .append(this.getType())
            .append(this.getValue())
            .append(this.getDescription())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
