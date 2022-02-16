package dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit;

import dev.sbs.simplifiedbot.optimizer.modules.common.Calculator;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import java.math.BigDecimal;
import java.util.Map;

public final class DamagePerHitCalculator extends Calculator<DamagePerHitItemEntity, DamagePerHitSolution> {

    @Override
    public SimpleBigDecimalScore calculateScore(DamagePerHitSolution solution) {
        Map<String, Double> computedStats = solution.getComputedStats();
        double strSum = this.getReforgeSum(solution, "STRENGTH") + computedStats.get("STRENGTH");
        double cdSum = this.getReforgeSum(solution, "CRITICAL_DAMAGE") + computedStats.get("CRITICAL_DAMAGE");
        double intermediateValue = (100 + strSum) * (100 + cdSum);
        return SimpleBigDecimalScore.of(BigDecimal.valueOf(intermediateValue));
    }

}
