package dev.sbs.simplifiedbot.optimizer.modules.damage_per_second;

import dev.sbs.simplifiedbot.optimizer.modules.common.Calculator;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import java.math.BigDecimal;
import java.util.Map;

public final class DamagePerSecondCalculator extends Calculator<DamagePerSecondItemEntity, DamagePerSecondSolution> {

    @Override
    public SimpleBigDecimalScore calculateScore(DamagePerSecondSolution solution) {
        Map<String, Double> computedStats = solution.getComputedStats();
        double strSum = this.getReforgeSum(solution, "STRENGTH") + computedStats.get("STRENGTH");
        double asSum = Math.min(this.getReforgeSum(solution, "ATTACK_SPEED") + computedStats.get("ATTACK_SPEED"), 82);
        double ferSum = this.getReforgeSum(solution, "FEROCITY") + computedStats.get("FEROCITY");
        double cdSum = this.getReforgeSum(solution, "CRITICAL_DAMAGE") + computedStats.get("CRITICAL_DAMAGE");
        double ccSum = Math.min(this.getReforgeSum(solution, "CRIT_CHANCE") + computedStats.get("CRIT_CHANCE"), 100);
        double intermediateValue = (100 + strSum) * (100 + ccSum) * (100 + asSum) * (100 + ferSum) * (100 + cdSum)
                - (100 + strSum) * (100 + ccSum) * (100 + asSum) * (100 + ferSum) * 100;
        return SimpleBigDecimalScore.of(BigDecimal.valueOf(intermediateValue));
    }

}
