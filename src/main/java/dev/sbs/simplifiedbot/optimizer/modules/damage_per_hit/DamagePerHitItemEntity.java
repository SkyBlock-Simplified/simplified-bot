package dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit;

import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ObjectData;
import dev.sbs.api.data.model.skyblock.reforge_types.ReforgeTypeModel;
import dev.sbs.simplifiedbot.optimizer.modules.common.ItemEntity;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeFact;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link PlanningEntity} to which Optaplanner allocates a {@link ReforgeFact}
 */
@PlanningEntity
public final class DamagePerHitItemEntity implements ItemEntity {

    @PlanningId
    @Getter private UUID uniqueId;
    @Getter private ReforgeTypeModel type;
    @Getter private ObjectData<?> objectData;

    @Getter
    @Setter
    @PlanningVariable(
        valueRangeProviderRefs = {"reforgeRange"},
        strengthComparatorClass = DamagePerHitReforgeComparator.class
    )
    private ReforgeFact reforgeFact;

    @Getter
    @ValueRangeProvider(id = "reforgeRange")
    @ProblemFactCollectionProperty
    private List<ReforgeFact> availableReforges;

    public DamagePerHitItemEntity() {
    } // Optimizer Cloning

    public DamagePerHitItemEntity(ReforgeTypeModel type, ObjectData<?> objectData, List<ReforgeFact> availableReforges) {
        this.uniqueId = UUID.randomUUID();
        this.type = type;
        this.objectData = objectData;
        this.availableReforges = availableReforges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DamagePerHitItemEntity that = (DamagePerHitItemEntity) o;
        return Objects.equals(this.getUniqueId(), that.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getUniqueId());
    }

    @Override
    public String toString() {
        return "DamagePerHitItemEntity(reforge=" + this.getAvailableReforges().get(0) + ")";
    }

    public boolean effectivelyEquals(DamagePerHitItemEntity that) {
        if (that == null) return false;
        return this.getType().equals(that.getType()) && this.getRarity().equals(that.getRarity());
    }

}
