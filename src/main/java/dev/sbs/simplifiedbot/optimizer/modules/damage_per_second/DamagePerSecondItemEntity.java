package dev.sbs.simplifiedbot.optimizer.modules.damage_per_second;

import dev.sbs.simplifiedbot.data.skyblock.item_types.ItemTypeModel;
import dev.sbs.simplifiedbot.optimizer.modules.common.ItemEntity;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeFact;
import dev.sbs.simplifiedbot.profile_stats.data.ObjectData;
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
@Getter
@PlanningEntity
public final class DamagePerSecondItemEntity implements ItemEntity {

    @PlanningId
    private UUID uniqueId;
    private ItemTypeModel type;
    private ObjectData<?> objectData;

    @Setter
    @PlanningVariable(
            valueRangeProviderRefs = {"reforgeRange"},
            strengthComparatorClass = DamagePerSecondReforgeComparator.class
    )
    private ReforgeFact reforgeFact;

    @ValueRangeProvider(id = "reforgeRange")
    @ProblemFactCollectionProperty
    private List<ReforgeFact> availableReforges;

    public DamagePerSecondItemEntity() {

    } // Optimizer Cloning

    public DamagePerSecondItemEntity(ItemTypeModel type, ObjectData<?> objectData, List<ReforgeFact> availableReforges) {
        this.uniqueId = UUID.randomUUID();
        this.type = type;
        this.objectData = objectData;
        this.availableReforges = availableReforges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DamagePerSecondItemEntity that = (DamagePerSecondItemEntity) o;
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

    public boolean effectivelyEquals(DamagePerSecondItemEntity that) {
        if (that == null) return false;
        return this.getType().equals(that.getType()) && this.getRarity().equals(that.getRarity());
    }

}
