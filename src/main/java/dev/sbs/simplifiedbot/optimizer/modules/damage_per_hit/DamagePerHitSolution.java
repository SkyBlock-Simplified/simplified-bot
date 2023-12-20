package dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit;

import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.profile_stats.data.ObjectData;
import dev.sbs.api.data.model.skyblock.item_types.ItemTypeModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.data.tuple.pair.Pair;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeFact;
import dev.sbs.simplifiedbot.optimizer.modules.common.Solution;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import java.util.List;

/**
 * Holds every {@link DamagePerHitItemEntity} and {@link ReforgeFact}
 */
@Getter
@PlanningSolution
public class DamagePerHitSolution extends Solution<DamagePerHitItemEntity> {

    @PlanningEntityCollectionProperty
    private List<DamagePerHitItemEntity> availableItems;

    @ProblemFactCollectionProperty
    private List<ReforgeFact> allReforges;

    @PlanningScore
    private SimpleBigDecimalScore score;

    protected DamagePerHitSolution() {

    } // Optimizer Cloning

    public DamagePerHitSolution(OptimizerRequest optimizerRequest) {
        super(optimizerRequest, Concurrent.newUnmodifiableList(DamagePerHitReforgeComparator.importantStatWeights.keySet()));

        // Generate Reforge Stats
        Pair<ConcurrentList<DamagePerHitItemEntity>, ConcurrentList<ReforgeFact>> availableContent = this.generateAvailableItems();
        this.availableItems = availableContent.getLeft();
        this.allReforges = availableContent.getRight();
    }

    @Override
    protected @NotNull DamagePerHitItemEntity createItemEntity(@NotNull ItemTypeModel itemTypeModel, @NotNull ObjectData<?> objectData, @NotNull ConcurrentList<ReforgeFact> optimalReforges) {
        return new DamagePerHitItemEntity(
            itemTypeModel,
            objectData,
            optimalReforges
        );
    }

}
