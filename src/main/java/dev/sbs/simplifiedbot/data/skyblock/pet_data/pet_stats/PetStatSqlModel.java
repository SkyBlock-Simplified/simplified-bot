package dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_stats;

import dev.sbs.api.data.model.SqlModel;
import dev.sbs.api.data.sql.converter.list.IntegerListConverter;
import dev.sbs.simplifiedbot.data.skyblock.pet_data.pets.PetSqlModel;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatSqlModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Getter
@Entity
@Table(
    name = "skyblock_pet_stats",
    indexes = {
        @Index(
            columnList = "pet_key, stat_key",
            unique = true
        ),
        @Index(
            columnList = "pet_key, ordinal",
            unique = true
        ),
        @Index(
            columnList = "pet_key"
        ),
        @Index(
            columnList = "stat_key"
        )
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PetStatSqlModel implements PetStatModel, SqlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "pet_key", referencedColumnName = "key", nullable = false)
    private PetSqlModel pet;

    @Setter
    @ManyToOne
    @JoinColumn(name = "stat_key", referencedColumnName = "key")
    private StatSqlModel stat;

    @Setter
    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;

    @Setter
    @Column(name = "rarities", nullable = false)
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> rarities;

    @Setter
    @Column(name = "base_value", nullable = false)
    private Double baseValue;

    @Setter
    @Column(name = "level_bonus", nullable = false)
    private Double levelBonus;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @UpdateTimestamp
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

}
