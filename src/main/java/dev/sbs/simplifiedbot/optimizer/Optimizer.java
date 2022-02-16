package dev.sbs.simplifiedbot.optimizer;

import dev.sbs.api.SimplifiedException;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
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
    private static final SolverManager<DamagePerHitSolution, UUID> damagePerHitSolver = OptimizerHelper.buildSolver(DamagePerHitItemEntity.class, DamagePerHitSolution.class, DamagePerHitCalculator.class);
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

            // Melee Damage
            double playerDamage = optimizerRequest.getPlayerStats().getCombinedStats().get(damageStatModel).getTotal();
            double weaponDamage = getWeaponDamage(optimizerRequest);
            double meleeDamage = playerDamage + weaponDamage;

            // Damage Multiplier
            double combatBonus = optimizerRequest.getPlayerStats().getDamageMultiplier();
            double enchantBonus = getEnchantBonus(optimizerRequest); // TODO: Requires Testing
            //enchantBonus += 0.84; // TODO: Ender Slayer VII
            //enchantBonus += 0.56; // TODO: Smite VII
            double weaponBonus = 0.0;
            double damageMultiplier = 1.0 + combatBonus + enchantBonus + weaponBonus;

            // Final Damage
            double armorBonus = 1.0 + getArmorBonus(optimizerRequest);
            double bonusDamage = meleeDamage * damageMultiplier * armorBonus;
            double finalDamage = score * bonusDamage / 10_000.0;
            // TODO: Hyperion   : 33,624 (Withered)
            // TODO: AOTD       : 9,707 (Withered) [No Enchants]
            // TODO: Hyperion   : 21,462 (Withered) [No Potions]
            // TODO: Hyperion   : 120,941 (Withered) [Accessories] [No Potions]
            // TODO: Hyperion   : 146,284 (Withered) [Accessories] [Potions]
            // TODO: Hyperion   : 254,034 (Withered) [Accessories] [Potions] [Necron Armor]
            // TODO: Hyperion   : 280,208 (Withered) [Accessories] [Potions] [Necron Armor] [Golden Dragon Pet]
            // TODO: Hyperion   : 404,452 (Withered) [Accessories] [Potions] [Necron Armor] [Ender Dragon Pet] [Zealot]
            // TODO: Hyperion   : 282,849 (Withered) [Accessories] [Potions] [Necron Armor] [Ender Dragon Pet] [Zombie] (263,820)

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
            PlayerStats calculatedPlayerStats = optimizerRequest.getPlayerStats();

            // Melee Damage
            double playerDamage = calculatedPlayerStats.getAllStats().get(damageStatModel).getTotal();
            double weaponDamage = getWeaponDamage(optimizerRequest);
            double meleeDamage = playerDamage + weaponDamage;

            // Damage Multiplier
            double combatBonus = calculatedPlayerStats.getDamageMultiplier();
            double enchantBonus = getEnchantBonus(optimizerRequest);
            enchantBonus += 0.84; // TODO: Ender Slayer VII
            double weaponBonus = 0.0;
            double damageMultiplier = 1.0 + combatBonus + enchantBonus + weaponBonus;

            // Final Damage
            double armorBonus = getArmorBonus(optimizerRequest);
            double bonusDamage = meleeDamage * damageMultiplier * (1.0 + armorBonus);
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
