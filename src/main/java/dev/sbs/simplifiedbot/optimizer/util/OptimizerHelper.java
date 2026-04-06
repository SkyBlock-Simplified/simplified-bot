package dev.sbs.simplifiedbot.optimizer.util;

import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.linked.ConcurrentLinkedMap;
import dev.simplified.collection.tuple.pair.Pair;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.persistence.model.BonusItemStat;
import dev.sbs.minecraftapi.persistence.model.BonusPetAbilityStat;
import dev.sbs.minecraftapi.persistence.model.Reforge;
import dev.sbs.minecraftapi.persistence.model.Stat;
import dev.sbs.simplifiedbot.optimizer.modules.common.Calculator;
import dev.sbs.simplifiedbot.optimizer.modules.common.ItemEntity;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeFact;
import dev.sbs.simplifiedbot.optimizer.modules.common.Solution;
import dev.sbs.simplifiedbot.profile_stats.data.Data;
import dev.sbs.simplifiedbot.profile_stats.data.PlayerDataHelper;
import org.jetbrains.annotations.NotNull;
import org.optaplanner.core.api.solver.SolverManager;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class OptimizerHelper {

    public static final Stat DAMAGE_STAT = MinecraftApi.getRepository(Stat.class).findFirstOrNull(Stat::getId, "DAMAGE");

    public static <I extends ItemEntity, S extends Solution<I>, C extends Calculator<I, S>> SolverManager<S, UUID> buildSolver(@NotNull Class<I> itemEntityClass, @NotNull Class<S> solutionClass, @NotNull Class<C> calculatorClass) {
        return OptimizerSolver.builder(itemEntityClass, solutionClass, calculatorClass)
            .withTerminationAfterSecondsUnimproved(3L)
            .withTerminationAfterSecondsSpent(10L)
            .build()
            .getManager();
    }

    protected static void debugRequest(@NotNull Solution<?> solution, @NotNull OptimizerRequest optimizerRequest) {
        // --- KNOWN PLAYER AND WEAPON STATS ---
        ConcurrentLinkedMap<String, Double> playerStats = optimizerRequest.getProfileStats()
            .getCombinedStats()
            .stream()
            .map(entry -> Pair.of(entry.getKey().getId(), entry.getValue().getTotal()))
            .collect(Concurrent.toLinkedMap());

        playerStats.forEach((key, value) -> System.out.println("Player: " + key + ": " + value));

        ConcurrentLinkedMap<String, Double> weaponStats = optimizerRequest.getWeapon()
            .map(weaponData -> weaponData.getAllStats()
                .stream()
                .map(entry -> Pair.of(entry.getKey().getId(), entry.getValue().getTotal()))
                .collect(Concurrent.toLinkedMap(Pair::getKey, Pair::getValue))
            )
            .orElse(Concurrent.newLinkedMap());

        weaponStats.forEach((key, value) -> System.out.println("Weapon: " + key + ": " + value));
        weaponStats.forEach(statEntry -> playerStats.put(statEntry.getKey(), playerStats.getOrDefault(statEntry.getKey(), 0.0) + statEntry.getValue()));
        playerStats.forEach((key, value) -> System.out.println("Player & Weapon: " + key + ": " + value));

        // --- OPTIMIZER PLAYER STATS ---
        ConcurrentLinkedMap<String, Double> stats = optimizerRequest.getProfileStats()
            .getCombinedStats(true)
            .stream()
            .map(entry -> Pair.of(entry.getKey().getId(), entry.getValue().getTotal()))
            .collect(Concurrent.toLinkedMap(Double::sum));


        optimizerRequest.getWeapon().ifPresent(weaponData -> weaponData.getStats()
            .stream()
            .filter(entry -> entry.getKey().isOptimizerConstant())
            .flatMap(entry -> entry.getValue()
                .stream()
                .map(subentry -> Pair.of(subentry.getKey().getId(), subentry.getValue().getTotal()))
            )
            .collect(Concurrent.toLinkedMap(Double::sum))
            .stream()
            .map(Pair::from)
            .forEach(statEntry -> stats.put(statEntry.getKey(), stats.getOrDefault(statEntry.getKey(), 0.0) + statEntry.getValue()))
        );

        solution.getAvailableItems()
            .stream()
            .map(ItemEntity::getReforgeFact)
            .forEach(reforgeFact -> reforgeFact.getEffects()
                .stream()
                .filter(entry -> stats.containsKey(entry.getKey()))
                .forEach(entry -> stats.put(entry.getKey(), stats.getOrDefault(entry.getKey(), 0.0) + entry.getValue()))
            );

        stats.forEach(statEntry -> System.out.println("Calculated: " + statEntry.getKey() + ": " + statEntry.getValue()));
    }

    public static double getArmorBonus(@NotNull OptimizerRequest optimizerRequest) {
        return optimizerRequest.getProfileStats()
            .getArmor()
            .stream()
            .flatMap(Optional::stream)
            .flatMapToDouble(armorData -> armorData
                .getBonusItemStatModels()
                .stream()
                .filter(BonusItemStat::isForStats)
                .filter(bonusItemStat -> bonusItemStat.noRequiredMobType() || optimizerRequest.getMobType().getKey().equals(bonusItemStat.getRequiredMobTypeKey()))
                .mapToDouble(bonusItemStat -> PlayerDataHelper.handleBonusEffects(
                    DAMAGE_STAT,
                    armorData.getAllData(DAMAGE_STAT).getTotal(),
                    armorData.getCompoundTag(),
                    optimizerRequest.getExpressionVariables(),
                    bonusItemStat
                ))
            )
            .sum();
    }

    public static double getPetAbilityBonus(@NotNull OptimizerRequest optimizerRequest) {
        return optimizerRequest.getProfileStats()
            .getBonusPetAbilityStatModels()
            .stream()
            .filter(BonusPetAbilityStat::isPercentage)
            .filter(bonusPetAbilityStat -> bonusPetAbilityStat.noRequiredItem() ||
                (optimizerRequest.getWeapon().isPresent() && optimizerRequest.getWeapon().get().getItem().equals(bonusPetAbilityStat.getRequiredItem()))
            )
            .filter(bonusPetAbilityStat -> bonusPetAbilityStat.noRequiredMobType() || bonusPetAbilityStat.getRequiredMobTypeKey().equals(optimizerRequest.getMobType().getKey()))
            .mapToDouble(bonusPetAbilityStat -> PlayerDataHelper.handleBonusEffects(
                DAMAGE_STAT,
                0.0,
                null,
                optimizerRequest.getExpressionVariables(),
                bonusPetAbilityStat
            ))
            .sum();
    }

    public static double getEnchantBonus(@NotNull OptimizerRequest optimizerRequest) {
        double enchantBonus = optimizerRequest.getProfileStats()
            .getArmor()
            .stream()
            .flatMap(Optional::stream)
            .flatMapToDouble(armorData -> armorData
                .getEnchantments()
                .stream()
                .filter(enchantmentEntry -> enchantmentEntry.getKey().getMobTypeIds().isEmpty() || enchantmentEntry.getKey().getMobTypeIds().contains(optimizerRequest.getMobType().getKey()))
                .flatMapToDouble(enchantmentEntry -> armorData.getEnchantmentStats()
                    .get(enchantmentEntry.getKey())
                    .stream()
                    .filter(sub -> sub.getType() == Stat.Type.PERCENT || sub.getType() == Stat.Type.PLUS_PERCENT) // Percentage Only
                    .filter(sub -> sub.getStat().isPresent() && DAMAGE_STAT.equals(sub.getStat().get())) // Damage Only
                    .filter(sub -> sub.getValues().keySet().stream().anyMatch(level -> enchantmentEntry.getValue() >= level)) // Contains Level
                    .mapToDouble(sub -> sub.getValues().entrySet().stream()
                        .filter(e -> enchantmentEntry.getValue() >= e.getKey())
                        .mapToDouble(Map.Entry::getValue)
                        .sum() / 100.0
                    )
                )
            )
            .sum();

        if (optimizerRequest.getWeapon().isPresent()) {
            OptimizerRequest.WeaponData weaponData = optimizerRequest.getWeapon().get();

            enchantBonus += weaponData.getEnchantments()
                .stream()
                .filter(enchantmentEntry -> enchantmentEntry.getKey().getMobTypeIds().isEmpty() || enchantmentEntry.getKey().getMobTypeIds().contains(optimizerRequest.getMobType().getKey()))
                .flatMapToDouble(enchantmentEntry -> weaponData.getEnchantmentStats()
                    .get(enchantmentEntry.getKey())
                    .stream()
                    .filter(sub -> sub.getType() == Stat.Type.PERCENT || sub.getType() == Stat.Type.PLUS_PERCENT) // Percentage Only
                    .filter(sub -> sub.getStat().isPresent() && DAMAGE_STAT.equals(sub.getStat().get())) // Damage Only
                    .filter(sub -> sub.getValues().keySet().stream().anyMatch(level -> enchantmentEntry.getValue() >= level)) // Contains Level
                    .mapToDouble(sub -> {
                        double value = sub.getValues().entrySet().stream()
                            .filter(e -> enchantmentEntry.getValue() >= e.getKey())
                            .mapToDouble(Map.Entry::getValue)
                            .sum() / 100.0;

                        System.out.println("Weapon adding: " + enchantmentEntry.getKey().getId() + ": " + value);
                        return value;
                    })
                )
                .sum();
        }

        return enchantBonus;
    }

    public static ConcurrentMap<Reforge, Integer> getReforgeCount(@NotNull Solution<?> solution) {
        return solution.getAvailableItems()
            .stream()
            .map(ItemEntity::getReforgeFact)
            .map(ReforgeFact::getReforge)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(it -> it))
            .entrySet()
            .stream()
            .collect(Concurrent.toMap(Map.Entry::getKey, o2 -> o2.getValue().size()));
    }

    public static double getWeaponBonus(@NotNull OptimizerRequest optimizerRequest) {
        return optimizerRequest.getWeapon()
            .stream()
            .flatMapToDouble(weaponData -> weaponData.getBonusItemStatModels()
                .stream()
                .filter(BonusItemStat::hasRequiredMobType)
                .filter(bonusItemStat -> optimizerRequest.getMobType().getKey().equals(bonusItemStat.getRequiredMobTypeKey()))
                .mapToDouble(bonusItemStat -> PlayerDataHelper.handleBonusEffects(
                    DAMAGE_STAT,
                    1.0,
                    weaponData.getCompoundTag(),
                    optimizerRequest.getExpressionVariables(),
                    bonusItemStat
                ))
            )
            .sum();
    }

    public static double getWeaponDamage(@NotNull OptimizerRequest optimizerRequest) {
        return optimizerRequest.getWeapon()
            .stream()
            .flatMap(weaponData -> weaponData.getStats().stream())
            .filter(entry -> entry.getKey().isOptimizerConstant())
            .map(Map.Entry::getValue)
            .map(map -> map.get(DAMAGE_STAT))
            .mapToDouble(Data::getTotal)
            .sum();
    }

}
