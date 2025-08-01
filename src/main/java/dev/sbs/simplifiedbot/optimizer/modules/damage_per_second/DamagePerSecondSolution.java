package dev.sbs.simplifiedbot.optimizer.modules.damage_per_second;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.stream.pair.Pair;
import dev.sbs.simplifiedbot.data.skyblock.item_types.ItemTypeModel;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeFact;
import dev.sbs.simplifiedbot.optimizer.modules.common.Solution;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import dev.sbs.simplifiedbot.profile_stats.data.ObjectData;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import java.util.List;

/**
 * Holds every {@link DamagePerSecondItemEntity} and {@link ReforgeFact}
 */
@Getter
@PlanningSolution
public class DamagePerSecondSolution extends Solution<DamagePerSecondItemEntity> {

    @PlanningEntityCollectionProperty
    private List<DamagePerSecondItemEntity> availableItems;

    @ProblemFactCollectionProperty
    private List<ReforgeFact> allReforges;

    @PlanningScore
    private SimpleBigDecimalScore score;

    protected DamagePerSecondSolution() {

    } // Optimizer Cloning

    public DamagePerSecondSolution(OptimizerRequest request) {
        super(request, Concurrent.newUnmodifiableList(DamagePerSecondReforgeComparator.importantStatWeights.keySet()));

        // Generate Reforge Stats
        Pair<ConcurrentList<DamagePerSecondItemEntity>, ConcurrentList<ReforgeFact>> availableContent = this.generateAvailableItems();
        this.availableItems = availableContent.getLeft();
        this.allReforges = availableContent.getRight();
    }

    @Override
    protected @NotNull DamagePerSecondItemEntity createItemEntity(@NotNull ItemTypeModel itemTypeModel, @NotNull ObjectData<?> objectData, @NotNull ConcurrentList<ReforgeFact> optimalReforges) {
        return new DamagePerSecondItemEntity(
            itemTypeModel,
            objectData,
            optimalReforges
        );
    }

}
