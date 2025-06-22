package dev.sbs.simplifiedbot.data.skyblock.formats;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.CacheExpiry;
import dev.sbs.api.data.sql.converter.ColorConverter;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Getter
@Entity
@Table(
    name = "skyblock_formats"
)
@CacheExpiry(length = TimeUnit.HOURS, value = 24)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FormatSqlModel implements FormatModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "code", nullable = false, length = 1)
    private char code;

    @Setter
    @Convert(converter = ColorConverter.class)
    @Column(name = "rgb", length = 1)
    private Color rgb;

    @Setter
    @Column(name = "format", nullable = false)
    private boolean format;

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

        FormatSqlModel that = (FormatSqlModel) o;

        return new EqualsBuilder()
            .append(this.getCode(), that.getCode())
            .append(this.isFormat(), that.isFormat())
            .append(this.getKey(), that.getKey())
            .append(this.getRgb(), that.getRgb())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getCode())
            .append(this.getRgb())
            .append(this.isFormat())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
