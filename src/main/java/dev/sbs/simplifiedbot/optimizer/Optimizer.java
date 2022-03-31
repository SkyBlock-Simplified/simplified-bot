package dev.sbs.simplifiedbot.optimizer;

import dev.sbs.api.util.SimplifiedException;
import dev.sbs.simplifiedbot.optimizer.exception.OptimizerException;
import dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit.DamagePerHitCalculator;
import dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit.DamagePerHitItemEntity;
import dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit.DamagePerHitSolution;
import dev.sbs.simplifiedbot.optimizer.modules.damage_per_second.DamagePerSecondSolution;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerHelper;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerResponse;
import org.jetbrains.annotations.NotNull;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class Optimizer extends OptimizerHelper {

    //private static final SolverManager<DamagePerHitSolution, UUID> damagePerHitSolver = SolverManager.create(SolverConfig.createFromXmlResource("optaplanner/damagePerHitSolver.xml"));
    private static final SolverManager<DamagePerHitSolution, UUID> damagePerHitSolver = buildSolver(DamagePerHitItemEntity.class, DamagePerHitSolution.class, DamagePerHitCalculator.class);
    private static final SolverManager<DamagePerSecondSolution, UUID> damagePerSecondSolver = SolverManager.create(SolverConfig.createFromXmlResource("optaplanner/damagePerSecondSolver.xml"));

    public static OptimizerResponse solve(@NotNull OptimizerRequest optimizerRequest) {
        return switch (optimizerRequest.getType()) {
            case DAMAGE_PER_HIT -> solveDamagePerHit(optimizerRequest);
            case DAMAGE_PER_SECOND -> solveDamagePerSecond(optimizerRequest);
        };
    }

    public static OptimizerResponse solveDamagePerHit(@NotNull OptimizerRequest optimizerRequest) {
        DamagePerHitSolution initialSolution = new DamagePerHitSolution(optimizerRequest);
        SolverJob<DamagePerHitSolution, UUID> solverJob = damagePerHitSolver.solve(UUID.randomUUID(), initialSolution);

        try {
            DamagePerHitSolution solution = solverJob.getFinalBestSolution();
            double score = solution.getScore().getScore().doubleValue();

            // Damage Multiplier
            double combatBonus = optimizerRequest.getPlayerStats().getDamageMultiplier();
            double enchantBonus = getEnchantBonus(optimizerRequest);
            double weaponBonus = 0.0;
            double damageMultiplier = 1.0 + (combatBonus + enchantBonus + weaponBonus);

            // Final Damage
            double armorBonus = getArmorBonus(optimizerRequest);
            double petBonus = getPetAbilityBonus(optimizerRequest);
            double bonusDamage = damageMultiplier * (1.0 + armorBonus + petBonus);
            double finalDamage = score * bonusDamage / 10_000.0;

            // hyperion - 428,221 / 425489.50 / 99.36%
            // midas - 483,179 / 469,698.45 / 97.21%
            // aotd - 208,955 / 206,992.69 / 99.06%
            // aotv - 228,802 / 226,406.65 / 98.95%
            // atomsplit (ofa) - 3,033,099 /
            // atomsplit - 1,982,977 /

            debugRequest(solution, optimizerRequest);

            return new OptimizerResponse(solution, getReforgeCount(solution), finalDamage, solverJob.getProblemId(), solverJob.getSolvingDuration());
        } catch (Exception exception) {
            throw SimplifiedException.of(OptimizerException.class)
                .withMessage("Optaplanner could not query solver job!")
                .withCause(exception)
                .build();
        }
    }

    public static OptimizerResponse solveDamagePerSecond(@NotNull OptimizerRequest optimizerRequest) {
        DamagePerSecondSolution initialSolution = new DamagePerSecondSolution(optimizerRequest);
        SolverJob<DamagePerSecondSolution, UUID> solverJob = damagePerSecondSolver.solve(UUID.randomUUID(), initialSolution);

        try {
            DamagePerSecondSolution solution = solverJob.getFinalBestSolution();
            double score = solution.getScore().getScore().doubleValue();

            // Damage Multiplier
            double combatBonus = optimizerRequest.getPlayerStats().getDamageMultiplier();
            double enchantBonus = getEnchantBonus(optimizerRequest);
            double weaponBonus = 0.0;
            double damageMultiplier = 1.0 + (combatBonus + enchantBonus + weaponBonus);

            // Final Damage
            double armorBonus = getArmorBonus(optimizerRequest);
            double petBonus = getPetAbilityBonus(optimizerRequest);
            double bonusDamage = damageMultiplier * (1.0 + armorBonus + petBonus);
            double finalDamage = 2 * score * bonusDamage / 10_000_000_000.0;

            return new OptimizerResponse(solution, getReforgeCount(solution), finalDamage, solverJob.getProblemId(), solverJob.getSolvingDuration());
        } catch (InterruptedException | ExecutionException exception) {
            throw SimplifiedException.of(OptimizerException.class)
                .withMessage("Optaplanner could not query solver job!")
                .withCause(exception)
                .build();
        }
    }

}
