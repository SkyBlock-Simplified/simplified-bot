package dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantments;

import dev.sbs.api.builder.EqualsBuilder;
import dev.sbs.api.builder.HashCodeBuilder;
import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.list.StringListConverter;
import dev.sbs.simplifiedbot.data.skyblock.enchantment_data.enchantment_families.EnchantmentFamilySqlModel;
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

@Getter
@Entity
@Table(
    name = "skyblock_enchantments",
    indexes = {
        @Index(
            columnList = "family_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EnchantmentSqlModel implements EnchantmentModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key")
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "family_key", referencedColumnName = "key", nullable = false)
    private EnchantmentFamilySqlModel family;

    @Setter
    @Column(name = "description", nullable = false)
    private String description;

    @Setter
    @Column(name = "mob_types", nullable = false)
    @Convert(converter = StringListConverter.class)
    private List<String> mobTypes;

    @Setter
    @Column(name = "required_level", nullable = false)
    private Integer requiredLevel;

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

        EnchantmentSqlModel that = (EnchantmentSqlModel) o;

        return new EqualsBuilder()
            .append(this.getKey(), that.getKey())
            .append(this.getName(), that.getName())
            .append(this.getFamily(), that.getFamily())
            .append(this.getDescription(), that.getDescription())
            .append(this.getMobTypes(), that.getMobTypes())
            .append(this.getRequiredLevel(), that.getRequiredLevel())
            .append(this.getUpdatedAt(), that.getUpdatedAt())
            .append(this.getSubmittedAt(), that.getSubmittedAt())
            .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.getKey())
            .append(this.getName())
            .append(this.getFamily())
            .append(this.getDescription())
            .append(this.getMobTypes())
            .append(this.getRequiredLevel())
            .append(this.getUpdatedAt())
            .append(this.getSubmittedAt())
            .build();
    }

}
