package dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit;

import dev.sbs.simplifiedbot.optimizer.modules.common.Calculator;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import java.math.BigDecimal;
import java.util.Map;

public final class DamagePerHitCalculator extends Calculator<DamagePerHitItemEntity, DamagePerHitSolution> {

    @Override
    public SimpleBigDecimalScore calculateScore(DamagePerHitSolution solution) {
        OptimizerRequest optimizerRequest = solution.getOptimizerRequest();
        Map<String, Double> computedStats = solution.getComputedStats();
        double strSum = this.getReforgeSum(solution, "STRENGTH") + computedStats.get("STRENGTH");
        double cdSum = this.getReforgeSum(solution, "CRITICAL_DAMAGE") + computedStats.get("CRITICAL_DAMAGE");
        double dmgSum = this.getReforgeSum(solution, "DAMAGE");
        double playerDamage = optimizerRequest.getPlayerDamage();
        double weaponDamage = optimizerRequest.getWeaponDamage();
        double weaponBonus = 1.0 + optimizerRequest.getWeaponBonus();
        double intermediateValue = (100 + strSum) * (100 + cdSum) * ((playerDamage + weaponDamage + dmgSum) * weaponBonus);
        return SimpleBigDecimalScore.of(BigDecimal.valueOf(intermediateValue));
    }

}
