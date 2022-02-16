package dev.sbs.simplifiedbot.optimizer.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.Data;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ItemData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.PlayerDataHelper;
import dev.sbs.api.data.model.skyblock.bonus_item_stats.BonusItemStatModel;
import dev.sbs.api.data.model.skyblock.enchantment_stats.EnchantmentStatModel;
import dev.sbs.api.data.model.skyblock.reforge_stats.ReforgeStatModel;
import dev.sbs.api.data.model.skyblock.stats.StatModel;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentMap;
import dev.sbs.api.util.helper.ListUtil;
import dev.sbs.simplifiedbot.optimizer.modules.common.Calculator;
import dev.sbs.simplifiedbot.optimizer.modules.common.ItemEntity;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeFact;
import dev.sbs.simplifiedbot.optimizer.modules.common.Solution;
import org.optaplanner.core.api.solver.SolverManager;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class OptimizerHelper {

    protected static final StatModel damageStatModel = SimplifiedApi.getRepositoryOf(StatModel.class).findFirstOrNull(StatModel::getKey, "DAMAGE");

    public static <I extends ItemEntity, S extends Solution<I>, C extends Calculator<I, S>> SolverManager<S, UUID> buildSolver(Class<I> itemEntityClass, Class<S> solutionClass, Class<C> calculatorClass) {
        return OptimizerSolver.builder(itemEntityClass, solutionClass, calculatorClass)
            .withTerminationAfterSecondsUnimproved(3L)
            .withTerminationAfterSecondsSpent(10L)
            .build()
            .getManager();
    }

    public static double getArmorBonus(OptimizerRequest optimizerRequest) {
        return optimizerRequest.getPlayerStats()
            .getArmor()
            .stream()
            .flatMapToDouble(armorData -> armorData
                .getBonusItemStatModel()
                .stream()
                .filter(BonusItemStatModel::isForStats)
                .mapToDouble(bonusItemStatModel -> PlayerDataHelper.handleBonusEffects(
                    damageStatModel,
                    armorData.getAllData(damageStatModel).getTotal(),
                    armorData.getCompoundTag(),
                    optimizerRequest.getExpressionVariables(),
                    bonusItemStatModel
                ))
            )
            .sum();
    }

    public static double getEnchantBonus(OptimizerRequest optimizerRequest) { // TODO: Requires Testing
        double enchantBonus = optimizerRequest.getPlayerStats()
            .getArmor()
            .stream()
            .flatMapToDouble(armorData -> armorData
                .getEnchantments()
                .stream()
                .filter(enchantmentEntry -> ListUtil.isEmpty(enchantmentEntry.getKey().getMobTypes()) || enchantmentEntry.getKey().getMobTypes().contains(optimizerRequest.getMobType().getKey()))
                .flatMapToDouble(enchantmentEntry -> armorData.getEnchantmentStats()
                    .get(enchantmentEntry.getKey())
                    .stream()
                    .filter(EnchantmentStatModel::isPercentage) // Percentage Only
                    .filter(enchantmentStatModel -> damageStatModel.equals(enchantmentStatModel.getStat())) // Damage Only
                    .filter(enchantmentStatModel -> enchantmentStatModel.getLevels().stream().anyMatch(level -> enchantmentEntry.getValue() >= level)) // Contains Level
                    .mapToDouble(enchantmentStatModel -> {
                        double levelBonus = enchantmentStatModel.getLevels().stream().filter(level -> enchantmentEntry.getValue() >= level).mapToDouble(__ -> enchantmentStatModel.getLevelBonus()).sum();
                        double percentage = (enchantmentStatModel.getBaseValue() + levelBonus) / 100.0;
                        System.out.println("Armor adding " + enchantmentStatModel.getEnchantment().getKey() + " with value " + percentage);
                        return percentage;
                    }))
            )
            .sum();

        if (optimizerRequest.getWeapon().isPresent()) {
            OptimizerRequest.Weapon weapon = optimizerRequest.getWeapon().get();
            ItemData weaponData = weapon.getItemData();

            enchantBonus += weaponData
                .getEnchantments()
                .stream()
                .filter(enchantmentEntry -> ListUtil.isEmpty(enchantmentEntry.getKey().getMobTypes()) || enchantmentEntry.getKey().getMobTypes().contains(optimizerRequest.getMobType().getKey()))
                .flatMapToDouble(enchantmentEntry -> weaponData
                    .getEnchantmentStats()
                    .get(enchantmentEntry.getKey())
                    .stream()
                    .filter(EnchantmentStatModel::isPercentage) // Percentage Only
                    .filter(enchantmentStatModel -> damageStatModel.equals(enchantmentStatModel.getStat())) // Damage Only
                    .filter(enchantmentStatModel -> enchantmentStatModel.getLevels().stream().anyMatch(level -> enchantmentEntry.getValue() >= level)) // Contains Level
                    .mapToDouble(enchantmentStatModel -> {
                        double levelBonus = enchantmentStatModel.getLevels().stream().filter(level -> enchantmentEntry.getValue() >= level).mapToDouble(__ -> enchantmentStatModel.getLevelBonus()).sum();
                        double percentage = (enchantmentStatModel.getBaseValue() + levelBonus) / 100.0;
                        System.out.println("Weapon adding " + enchantmentStatModel.getEnchantment().getKey() + " with value " + percentage);
                        return percentage;
                    })
                )
                .sum();
        }

        return enchantBonus;
    }

    public static ConcurrentMap<ReforgeStatModel, Integer> getReforgeCount(Solution<?> solution) {
        return solution.getAvailableItems()
            .stream()
            .map(ItemEntity::getReforgeFact)
            .map(ReforgeFact::getReforge)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(it -> it))
            .entrySet()
            .stream()
            .collect(Concurrent.toMap(Map.Entry::getKey, it -> it.getValue().size()));
    }

    public static double getWeaponDamage(OptimizerRequest optimizerRequest) {
        return optimizerRequest.getWeapon().map(weapon -> weapon.getItemData().getAllStats().get(damageStatModel)).map(Data::getTotal).orElse(0.0);
    }

}
