package dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit;

import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ObjectData;
import dev.sbs.api.data.model.skyblock.reforge_types.ReforgeTypeModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.data.tuple.Pair;
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
 * Holds every {@link DamagePerHitItemEntity} and {@link ReforgeFact}
 */
@PlanningSolution
public class DamagePerHitSolution extends Solution<DamagePerHitItemEntity> {

    @Getter
    @PlanningEntityCollectionProperty
    private List<DamagePerHitItemEntity> availableItems;

    @Getter
    @ProblemFactCollectionProperty
    private List<ReforgeFact> allReforges;

    @Getter
    @PlanningScore
    private SimpleBigDecimalScore score;

    protected DamagePerHitSolution() {

    } // Optimizer Cloning

    public DamagePerHitSolution(OptimizerRequest optimizerRequest) {
        super(optimizerRequest, Concurrent.newUnmodifiableList(DamagePerHitReforgeComparator.importantStatWeights.keySet()));

        // Generate Reforge Stats
        Pair<ConcurrentList<DamagePerHitItemEntity>, ConcurrentList<ReforgeFact>> availableContent = this.generateAvailableItems();
        this.availableItems = availableContent.getLeft();
        this.allReforges =availableContent.getRight();
    }

    @Override
    protected DamagePerHitItemEntity createItemEntity(ReforgeTypeModel reforgeTypeModel, ObjectData<?> objectData, ConcurrentList<ReforgeFact> optimalReforges) {
        return new DamagePerHitItemEntity(
            reforgeTypeModel,
            objectData,
            optimalReforges
        );
    }

}
