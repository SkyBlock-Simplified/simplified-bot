package dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_abilities;

import dev.sbs.api.data.model.SqlModel;
import dev.sbs.simplifiedbot.data.skyblock.pet_data.pets.PetSqlModel;
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
    name = "skyblock_pet_abilities",
    indexes = {
        @Index(
            columnList = "pet_key, ordinal",
            unique = true
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PetAbilitySqlModel implements PetAbilityModel, SqlModel {

    @Id
    @Setter
    @Column(name = "key", nullable = false)
    private String key;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "pet_key", referencedColumnName = "key", nullable = false)
    private PetSqlModel pet;

    @Setter
    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;

    @Setter
    @Column(name = "description", nullable = false)
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @UpdateTimestamp
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

}
