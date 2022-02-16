package dev.sbs.simplifiedbot.optimizer.modules.damage_per_second;

import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ObjectData;
import dev.sbs.api.data.model.skyblock.reforge_types.ReforgeTypeModel;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentList;
import dev.sbs.api.util.tuple.Pair;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeFact;
import dev.sbs.simplifiedbot.optimizer.modules.common.Solution;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import lombok.Getter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import java.util.List;

/**
 * Holds every {@link DamagePerSecondItemEntity} and {@link ReforgeFact}
 */
@PlanningSolution
public class DamagePerSecondSolution extends Solution<DamagePerSecondItemEntity> {

    @Getter
    @PlanningEntityCollectionProperty
    private List<DamagePerSecondItemEntity> availableItems;

    @Getter
    @ProblemFactCollectionProperty
    private List<ReforgeFact> allReforges;

    @Getter
    @PlanningScore
    private SimpleBigDecimalScore score;

    protected DamagePerSecondSolution() {

    } // Optimizer Cloning

    public DamagePerSecondSolution(OptimizerRequest request) {
        super(request, Concurrent.newUnmodifiableList(DamagePerSecondReforgeComparator.importantStatWeights.keySet()));

        // Generate Reforge Stats
        Pair<ConcurrentList<DamagePerSecondItemEntity>, ConcurrentList<ReforgeFact>> availableContent = this.generateAvailableItems();
        this.availableItems = availableContent.getLeft();
        this.allReforges =availableContent.getRight();
    }

    @Override
    protected DamagePerSecondItemEntity createItemEntity(ReforgeTypeModel reforgeTypeModel, ObjectData<?> objectData, ConcurrentList<ReforgeFact> optimalReforges) {
        return new DamagePerSecondItemEntity(
            reforgeTypeModel,
            objectData,
            optimalReforges
        );
    }

}
