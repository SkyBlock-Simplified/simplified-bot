package dev.sbs.simplifiedbot.optimizer;

import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
import dev.sbs.api.util.SimplifiedException;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.linked.ConcurrentLinkedMap;
import dev.sbs.api.util.data.tuple.Pair;
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
            //enchantBonus -= 0.6625; // TODO: Hyperion: Remove Execute and Giant Killer
            enchantBonus -= 0.457; // TODO: Midas: Remove Prosecute and Giant Killer
            //enchantBonus -= 0.3; // TODO: AOTD: Remove Giant Killer
            double weaponBonus = 0.0;
            double damageMultiplier = 1.0 + (combatBonus + enchantBonus + weaponBonus);

            // Final Damage
            double armorBonus = getArmorBonus(optimizerRequest);
            double petBonus = getPetAbilityBonus(optimizerRequest);
            double bonusDamage = damageMultiplier * (1.0 + armorBonus + petBonus);
            double finalDamage = score * bonusDamage / 10_000.0;

            // TODO: --- KNOWN PLAYER AND WEAPON STATS ---
            ConcurrentLinkedMap<String, Double> playerStats = optimizerRequest.getPlayerStats()
                .getCombinedStats()
                .stream()
                .filter(entry -> entry.getKey().getOrdinal() <= 6)
                .map(entry -> Pair.of(entry.getKey().getKey(), entry.getValue().getTotal()))
                .collect(Concurrent.toLinkedMap());

            playerStats.forEach((key, value) -> System.out.println("Player: " + key + ": " + value));

            ConcurrentLinkedMap<String, Double> weaponStats = optimizerRequest.getWeapon()
                .get()
                .getAllStats()
                .stream()
                .filter(entry -> entry.getKey().getOrdinal() <= 6)
                .map(entry -> Pair.of(entry.getKey().getKey(), entry.getValue().getTotal()))
                .collect(Concurrent.toLinkedMap());

            weaponStats.forEach((key, value) -> System.out.println("Weapon: " + key + ": " + value));
            weaponStats.forEach(statEntry -> playerStats.put(statEntry.getKey(), playerStats.getOrDefault(statEntry.getKey(), 0.0) + statEntry.getValue()));
            playerStats.forEach((key, value) -> System.out.println("Player & Weapon: " + key + ": " + value));
            // TODO: --- KNOWN PLAYER AND WEAPON STATS ---

            // TODO: --- OPTIMIZER PLAYER STATS ---
            ConcurrentLinkedMap<String, Double> stats = optimizerRequest.getPlayerStats()
                .getCombinedStats(true)
                .stream()
                .filter(entry -> entry.getKey().getOrdinal() <= 6)
                .map(entry -> Pair.of(entry.getKey().getKey(), entry.getValue().getTotal()))
                .collect(Concurrent.toLinkedMap(Double::sum));

            optimizerRequest.getWeapon()
                .get()
                .getStats()
                .stream()
                .filter(entry -> entry.getKey().isOptimizerConstant())
                .flatMap(entry -> entry.getValue()
                    .stream()
                    .filter(subentry -> subentry.getKey().getOrdinal() <= 6)
                    .map(subentry -> Pair.of(subentry.getKey().getKey(), subentry.getValue().getTotal()))
                )
                .collect(Concurrent.toLinkedMap(Double::sum))
                .stream()
                .map(entry -> Pair.of((String)entry.getKey(), entry.getValue()))
                .forEach(statEntry -> stats.put(statEntry.getKey(), stats.getOrDefault(statEntry.getKey(), 0.0) + statEntry.getValue()));

            solution.getAvailableItems()
                .stream()
                .map(DamagePerHitItemEntity::getReforgeFact)
                .forEach(reforgeFact -> reforgeFact.getEffects()
                    .stream()
                    .filter(entry -> stats.containsKey(entry.getKey()))
                    .forEach(entry -> stats.put(entry.getKey(), stats.getOrDefault(entry.getKey(), 0.0) + entry.getValue()))
                );

            stats.forEach(statEntry -> System.out.println("Calculated: " + statEntry.getKey() + ": " + statEntry.getValue()));
            // TODO: --- OPTIMIZER PLAYER STATS ---

            // Weapon: Midas
            // Damage: 581,175
            // Adjusted: 511,042.23 (87.93%)

            // Weapon: Midas
            // Pet: None
            // Damage: 359,946
            // Adjusted: 315,868.97 (87.76%)

            // Weapon: Midas
            // Pet: None
            // Armor: None
            // Damage: 197,659
            // Adjusted: 173,255.44 (87.65%)

            // TODO: Working On This
            // Weapon: Midas
            // Pet: None
            // Armor: None
            // Accessories: None
            // Damage: 41,524
            // Adjusted: 35,846.69 (86.33%)

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
            double playerDamage = calculatedPlayerStats.getAllStats().get(DAMAGE_STAT_MODEL).getTotal();
            double weaponDamage = getWeaponDamage(optimizerRequest);
            double meleeDamage = playerDamage + weaponDamage;

            // Damage Multiplier
            double combatBonus = calculatedPlayerStats.getDamageMultiplier();
            double enchantBonus = getEnchantBonus(optimizerRequest);
            double weaponBonus = 0.0;
            double damageMultiplier = 1.0 + combatBonus + enchantBonus + weaponBonus;

            // Final Damage
            double armorBonus = 1.0 + getArmorBonus(optimizerRequest);
            double bonusDamage = meleeDamage * damageMultiplier * armorBonus;
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
