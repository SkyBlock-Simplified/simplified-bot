package dev.sbs.simplifiedbot.model;

import dev.sbs.api.persistence.JpaModel;
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
import java.util.Objects;

@Getter
@Entity
@Table(
    name = "discord_settings",
    indexes = {
        @Index(
            columnList = "type_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AppSetting implements JpaModel {

    @Id
    @Setter
    @Column(name = "key", nullable = false, unique = true)
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "type_key", referencedColumnName = "key", nullable = false)
    private AppSettingType type;

    @Setter
    @Column(name = "value", nullable = false)
    private String value;

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

        AppSetting that = (AppSetting) o;

        return Objects.equals(this.getKey(), that.getKey())
            && Objects.equals(this.getName(), that.getName())
            && Objects.equals(this.getType(), that.getType())
            && Objects.equals(this.getValue(), that.getValue())
            && Objects.equals(this.getUpdatedAt(), that.getUpdatedAt())
            && Objects.equals(this.getSubmittedAt(), that.getSubmittedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getKey(), this.getName(), this.getType(), this.getValue(), this.getUpdatedAt(), this.getSubmittedAt());
    }

}