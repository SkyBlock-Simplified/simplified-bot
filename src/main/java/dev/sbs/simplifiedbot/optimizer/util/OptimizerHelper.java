package dev.sbs.simplifiedbot.optimizer.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.profile_stats.data.Data;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.profile_stats.data.PlayerDataHelper;
import dev.sbs.api.data.model.skyblock.bonus_data.bonus_item_stats.BonusItemStatModel;
import dev.sbs.api.data.model.skyblock.bonus_data.bonus_pet_ability_stats.BonusPetAbilityStatModel;
import dev.sbs.api.data.model.skyblock.enchantment_data.enchantment_stats.EnchantmentStatModel;
import dev.sbs.api.data.model.skyblock.reforge_data.reforge_stats.ReforgeStatModel;
import dev.sbs.api.data.model.skyblock.stats.StatModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.collection.concurrent.linked.ConcurrentLinkedMap;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.ListUtil;
import dev.sbs.simplifiedbot.optimizer.modules.common.Calculator;
import dev.sbs.simplifiedbot.optimizer.modules.common.ItemEntity;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeFact;
import dev.sbs.simplifiedbot.optimizer.modules.common.Solution;
import org.jetbrains.annotations.NotNull;
import org.optaplanner.core.api.solver.SolverManager;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class OptimizerHelper {

    public static final StatModel DAMAGE_STAT_MODEL = SimplifiedApi.getRepositoryOf(StatModel.class).findFirstOrNull(StatModel::getKey, "DAMAGE");

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
            .filter(entry -> entry.getKey().getOrdinal() <= 6)
            .map(entry -> Pair.of(entry.getKey().getKey(), entry.getValue().getTotal()))
            .collect(Concurrent.toLinkedMap());

        playerStats.forEach((key, value) -> System.out.println("Player: " + key + ": " + value));

        ConcurrentLinkedMap<String, Double> weaponStats = optimizerRequest.getWeapon()
            .map(weaponData -> weaponData.getAllStats()
                .stream()
                .filter(entry -> entry.getKey().getOrdinal() <= 6)
                .map(entry -> Pair.of(entry.getKey().getKey(), entry.getValue().getTotal()))
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
            .filter(entry -> entry.getKey().getOrdinal() <= 6)
            .map(entry -> Pair.of(entry.getKey().getKey(), entry.getValue().getTotal()))
            .collect(Concurrent.toLinkedMap(Double::sum));


        optimizerRequest.getWeapon().ifPresent(weaponData -> weaponData.getStats()
            .stream()
            .filter(entry -> entry.getKey().isOptimizerConstant())
            .flatMap(entry -> entry.getValue()
                .stream()
                .filter(subentry -> subentry.getKey().getOrdinal() <= 6)
                .map(subentry -> Pair.of(subentry.getKey().getKey(), subentry.getValue().getTotal()))
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
                .filter(BonusItemStatModel::isForStats)
                .filter(bonusItemStatModel -> bonusItemStatModel.noRequiredMobType() || optimizerRequest.getMobType().equals(bonusItemStatModel.getRequiredMobType()))
                .mapToDouble(bonusItemStatModel -> PlayerDataHelper.handleBonusEffects(
                    DAMAGE_STAT_MODEL,
                    armorData.getAllData(DAMAGE_STAT_MODEL).getTotal(),
                    armorData.getCompoundTag(),
                    optimizerRequest.getExpressionVariables(),
                    bonusItemStatModel
                ))
            )
            .sum();
    }

    public static double getPetAbilityBonus(@NotNull OptimizerRequest optimizerRequest) {
        return optimizerRequest.getProfileStats()
            .getBonusPetAbilityStatModels()
            .stream()
            .filter(BonusPetAbilityStatModel::isPercentage)
            .filter(bonusPetAbilityStatModel -> bonusPetAbilityStatModel.noRequiredItem() ||
                (optimizerRequest.getWeapon().isPresent() && optimizerRequest.getWeapon().get().getItem().equals(bonusPetAbilityStatModel.getRequiredItem()))
            )
            .filter(bonusPetAbilityStatModel -> bonusPetAbilityStatModel.noRequiredMobType() || bonusPetAbilityStatModel.getRequiredMobType().equals(optimizerRequest.getMobType()))
            .mapToDouble(bonusPetAbilityStatModel -> PlayerDataHelper.handleBonusEffects(
                DAMAGE_STAT_MODEL,
                0.0,
                null,
                optimizerRequest.getExpressionVariables(),
                bonusPetAbilityStatModel
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
                .filter(enchantmentEntry -> ListUtil.isEmpty(enchantmentEntry.getKey().getMobTypes()) || enchantmentEntry.getKey().getMobTypes().contains(optimizerRequest.getMobType().getKey()))
                .flatMapToDouble(enchantmentEntry -> armorData.getEnchantmentStats()
                    .get(enchantmentEntry.getKey())
                    .stream()
                    .filter(EnchantmentStatModel::isPercentage) // Percentage Only
                    .filter(enchantmentStatModel -> DAMAGE_STAT_MODEL.equals(enchantmentStatModel.getStat())) // Damage Only
                    .filter(enchantmentStatModel -> enchantmentStatModel.getLevels().stream().anyMatch(level -> enchantmentEntry.getValue() >= level)) // Contains Level
                    .mapToDouble(enchantmentStatModel -> (enchantmentStatModel.getBaseValue() + enchantmentStatModel.getLevels()
                        .stream()
                        .filter(level -> enchantmentEntry.getValue() >= level)
                        .mapToDouble(__ -> enchantmentStatModel.getLevelBonus())
                        .sum()) / 100.0
                    )
                )
            )
            .sum();

        if (optimizerRequest.getWeapon().isPresent()) {
            OptimizerRequest.WeaponData weaponData = optimizerRequest.getWeapon().get();

            enchantBonus += weaponData.getEnchantments()
                .stream()
                .filter(enchantmentEntry -> ListUtil.isEmpty(enchantmentEntry.getKey().getMobTypes()) || enchantmentEntry.getKey().getMobTypes().contains(optimizerRequest.getMobType().getKey()))
                .flatMapToDouble(enchantmentEntry -> weaponData.getEnchantmentStats()
                    .get(enchantmentEntry.getKey())
                    .stream()
                    .filter(EnchantmentStatModel::isPercentage) // Percentage Only
                    .filter(enchantmentStatModel -> DAMAGE_STAT_MODEL.equals(enchantmentStatModel.getStat())) // Damage Only
                    .filter(enchantmentStatModel -> enchantmentStatModel.getLevels().stream().anyMatch(level -> enchantmentEntry.getValue() >= level)) // Contains Level
                    .mapToDouble(enchantmentStatModel -> {
                        double value = (enchantmentStatModel.getBaseValue() + enchantmentStatModel.getLevels()
                            .stream()
                            .filter(level -> enchantmentEntry.getValue() >= level)
                            .mapToDouble(__ -> enchantmentStatModel.getLevelBonus())
                            .sum()) / 100.0;

                        System.out.println("Weapon adding: " + enchantmentStatModel.getEnchantment().getKey() + ": " + value);
                        return value;
                    })
                )
                .sum();
        }

        return enchantBonus;
    }

    public static ConcurrentMap<ReforgeStatModel, Integer> getReforgeCount(@NotNull Solution<?> solution) {
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
                .filter(BonusItemStatModel::hasRequiredMobType)
                .filter(bonusItemStatModel -> optimizerRequest.getMobType().equals(bonusItemStatModel.getRequiredMobType()))
                .mapToDouble(bonusItemStatModel -> PlayerDataHelper.handleBonusEffects(
                    DAMAGE_STAT_MODEL,
                    1.0,
                    weaponData.getCompoundTag(),
                    optimizerRequest.getExpressionVariables(),
                    bonusItemStatModel
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
            .map(map -> map.get(DAMAGE_STAT_MODEL))
            .mapToDouble(Data::getTotal)
            .sum();
    }

}
