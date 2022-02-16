package dev.sbs.simplifiedbot.optimizer.modules.common;

import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

import java.util.Objects;

public abstract class Calculator<I extends ItemEntity, T extends Solution<I>> implements EasyScoreCalculator<T, SimpleBigDecimalScore> {

    protected final double getReforgeSum(T solution, String stat) {
        return solution.getAvailableItems()
            .stream()
            .map(I::getReforgeFact)
            .filter(Objects::nonNull)
            .mapToDouble(reforge -> reforge.getEffect(stat))
            .sum();
    }

}
