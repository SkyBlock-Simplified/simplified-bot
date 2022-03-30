package dev.sbs.simplifiedbot.optimizer.modules.common;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.AccessoryData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ItemData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ObjectData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.PlayerDataHelper;
import dev.sbs.api.data.model.skyblock.bonus_item_stats.BonusItemStatModel;
import dev.sbs.api.data.model.skyblock.bonus_pet_ability_stats.BonusPetAbilityStatModel;
import dev.sbs.api.data.model.skyblock.bonus_reforge_stats.BonusReforgeStatModel;
import dev.sbs.api.data.model.skyblock.reforge_conditions.ReforgeConditionModel;
import dev.sbs.api.data.model.skyblock.reforge_stats.ReforgeStatModel;
import dev.sbs.api.data.model.skyblock.reforge_types.ReforgeTypeModel;
import dev.sbs.api.data.model.skyblock.stats.StatModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.data.mutable.MutableDouble;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import lombok.Getter;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class Solution<T extends ItemEntity> {

    @Getter private final ConcurrentList<ReforgeStatModel> allReforgeStatModels = SimplifiedApi.getRepositoryOf(ReforgeStatModel.class).findAll();
    @Getter private final Map<String, Double> computedStats = new ConcurrentMap<>();
    @Getter private PlayerStats playerStats;
    @Getter private OptimizerRequest optimizerRequest;
    @Getter private ConcurrentList<StatModel> importantStats;

    protected Solution() {

    } // Optimizer Cloning

    protected Solution(OptimizerRequest optimizerRequest, ConcurrentList<StatModel> importantStats) {
        this.optimizerRequest = optimizerRequest;
        this.importantStats = importantStats;
        this.playerStats = optimizerRequest.getPlayerStats();

        // Handle Constant Stats
        this.getImportantStats().forEach(statModel -> {
            MutableDouble value = new MutableDouble();
            value.add(getConstSum(this.getPlayerStats(), statModel)); // Add Player, Accessory and Item Stats
            this.getOptimizerRequest().getWeapon().ifPresent(weaponData -> value.add(getConstSum(weaponData, statModel))); // Add Weapon Stats
            this.computedStats.put(statModel.getKey(), value.get());
        });

        String stop = "here";
    }

    protected abstract T createItemEntity(ReforgeTypeModel reforgeTypeModel, ObjectData<?> objectData, ConcurrentList<ReforgeFact> optimalReforges);

    /**
     * Finds the applicable {@link ReforgeFact}s for each {@link ItemEntity}.
     *
     * @return {@link Pair} holding lists of all the {@link ItemEntity ItemEntities} and {@link ReforgeFact ReforgeFacts}.
     */
    protected Pair<ConcurrentList<T>, ConcurrentList<ReforgeFact>> generateAvailableItems() {
        ConcurrentList<T> availableItems = Concurrent.newList();
        ReforgeTypeModel accessoryReforgeTypeModel = SimplifiedApi.getRepositoryOf(ReforgeTypeModel.class).findFirstOrNull(ReforgeTypeModel::getKey, "ACCESSORY");
        ReforgeTypeModel armorReforgeTypeModel = SimplifiedApi.getRepositoryOf(ReforgeTypeModel.class).findFirstOrNull(ReforgeTypeModel::getKey, "ARMOR");
        ReforgeTypeModel swordReforgeTypeModel = SimplifiedApi.getRepositoryOf(ReforgeTypeModel.class).findFirstOrNull(ReforgeTypeModel::getKey, "SWORD");

        // Handle Accessories
        this.getOptimizerRequest()
            .getPlayerStats()
            .getFilteredAccessories()
            .forEach(accessoryData -> availableItems.add(this.getOptimalReforges(
                accessoryReforgeTypeModel,
                accessoryData
            )));

        // Handle Armor
        this.getOptimizerRequest()
            .getPlayerStats()
            .getArmor()
            .stream()
            .flatMap(Optional::stream)
            .forEach(itemData -> availableItems.add(this.getOptimalReforges(
                armorReforgeTypeModel,
                itemData
            )));

        // Handle Weapon
        this.getOptimizerRequest()
            .getWeapon()
            .ifPresent(weaponData -> availableItems.add(this.getOptimalReforges(
                swordReforgeTypeModel,
                weaponData
            )));

        return Pair.of(availableItems, availableItems.stream()
            .flatMap(itemEntity -> itemEntity.getAvailableReforges().stream())
            .collect(Concurrent.toList())
        );
    }

    public abstract List<ReforgeFact> getAllReforges();

    public abstract List<T> getAvailableItems();

    public static double getConstSum(PlayerStats playerStats, StatModel statModel) {
        double total = 0.0;

        // Player Stats
        total += Arrays.stream(PlayerStats.Type.values())
            .filter(PlayerStats.Type::isOptimizerConstant)
            .mapToDouble(type -> playerStats.getStatsOf(type).get(statModel).getTotal())
            .sum();

        // Armor Stats
        total += playerStats.getArmor()
            .stream()
            .flatMap(Optional::stream)
            .mapToDouble(armorData -> getDataSum(armorData, ObjectData.Type::isOptimizerConstant, statModel, ItemData.Type.values()))
            .sum();

        // Accessory Stats
        total += playerStats.getFilteredAccessories()
            .stream()
            .mapToDouble(accessoryData -> getDataSum(accessoryData, ObjectData.Type::isOptimizerConstant, statModel, AccessoryData.Type.values()))
            .sum();

        return total;
    }

    public static double getConstSum(OptimizerRequest.WeaponData weaponData, StatModel statModel) {
        return getDataSum(weaponData, ObjectData.Type::isOptimizerConstant, statModel, ItemData.Type.values());
    }

    private static <T extends ObjectData.Type> double getDataSum(ObjectData<T> objectData, Function<ObjectData.Type, Boolean> comparator, StatModel statModel, T[] values) {
        return Arrays.stream(values)
            .filter(ObjectData.Type::isOptimizerConstant)
            .mapToDouble(type -> objectData.getData(statModel, type).getTotal())
            .sum();
    }

    private T getOptimalReforges(ReforgeTypeModel reforgeTypeModel, ObjectData<?> objectData) {
        // Filter by Rarity and Allowed Reforge Stones
        ConcurrentMap<ReforgeStatModel, Boolean> optimalReforges = this.getAllReforgeStatModels()
            .stream()
            .filter(reforgeStatModel -> reforgeStatModel.getReforge().getType().equals(reforgeTypeModel))
            .filter(reforgeStatModel -> reforgeStatModel.getRarity().equals(objectData.getRarity()))
            .filter(reforgeStatModel -> this.getOptimizerRequest().getAllowedReforges().contains(reforgeStatModel))
            .filter(reforgeStatModel -> {
                for (ReforgeConditionModel reforgeConditionModel : reforgeStatModel.getReforge().getConditions()) {
                    if (!reforgeConditionModel.getItem().equals(objectData.getItem()))
                        return false;
                }

                return true;
            })
            .map(reforgeStatModel -> Pair.of(reforgeStatModel, false))
            .collect(Concurrent.toMap());

        // Compare Reforges
        for (ReforgeStatModel thisReforgeStatModel : optimalReforges.keySet()) {
            boolean thisIsWorseAtEveryStat = true;
            Optional<BonusReforgeStatModel> optionalThisBonusReforgeStatModel = SimplifiedApi.getRepositoryOf(BonusReforgeStatModel.class)
                .findFirst(BonusReforgeStatModel::getReforge, thisReforgeStatModel.getReforge());

            for (ReforgeStatModel thatReforgeStatModel : optimalReforges.keySet()) {
                Optional<BonusReforgeStatModel> optionalThatBonusReforgeStatModel = SimplifiedApi.getRepositoryOf(BonusReforgeStatModel.class)
                    .findFirst(BonusReforgeStatModel::getReforge, thatReforgeStatModel.getReforge());

                for (StatModel statModel : this.getImportantStats()) {
                    double thisStat = thisReforgeStatModel.getEffects().getOrDefault(statModel.getKey(), 0.0);
                    double thatStat = thatReforgeStatModel.getEffects().getOrDefault(statModel.getKey(), 0.0);

                    // Handle Bonus Reforge Effects
                    if (optionalThisBonusReforgeStatModel.isPresent()) {
                        BonusReforgeStatModel thisBonusReforgeStatModel = optionalThisBonusReforgeStatModel.get();

                        thisStat = PlayerDataHelper.handleBonusEffects(
                            statModel,
                            thisStat,
                            objectData.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            thisBonusReforgeStatModel
                        );
                    }

                    if (optionalThatBonusReforgeStatModel.isPresent()) {
                        BonusReforgeStatModel thatBonusReforgeStatModel = optionalThatBonusReforgeStatModel.get();

                        thatStat = PlayerDataHelper.handleBonusEffects(
                            statModel,
                            thatStat,
                            objectData.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            thatBonusReforgeStatModel
                        );
                    }

                    // Handle Bonus Item Effects
                    if (objectData.getBonusItemStatModel().isPresent()) {
                        BonusItemStatModel bonusItemStatModel = objectData.getBonusItemStatModel().get();

                        if (bonusItemStatModel.isForReforges()) {
                            thisStat = PlayerDataHelper.handleBonusEffects(
                                statModel,
                                thisStat,
                                objectData.getCompoundTag(),
                                this.getOptimizerRequest().getExpressionVariables(),
                                bonusItemStatModel
                            );

                            thatStat = PlayerDataHelper.handleBonusEffects(
                                statModel,
                                thatStat,
                                objectData.getCompoundTag(),
                                this.getOptimizerRequest().getExpressionVariables(),
                                bonusItemStatModel
                            );
                        }
                    }

                    // Handle Bonus Pet Ability Effects
                    for (BonusPetAbilityStatModel bonusPetAbilityStatModel : this.getPlayerStats().getBonusPetAbilityStatModels()) {
                        if (bonusPetAbilityStatModel.isPercentage()) {
                            if (bonusPetAbilityStatModel.noRequiredItem() && bonusPetAbilityStatModel.noRequiredMobType()) {
                                thisStat = PlayerDataHelper.handleBonusEffects(
                                    statModel,
                                    thisStat,
                                    objectData.getCompoundTag(),
                                    this.getOptimizerRequest().getExpressionVariables(),
                                    bonusPetAbilityStatModel
                                );

                                thatStat = PlayerDataHelper.handleBonusEffects(
                                    statModel,
                                    thatStat,
                                    objectData.getCompoundTag(),
                                    this.getOptimizerRequest().getExpressionVariables(),
                                    bonusPetAbilityStatModel
                                );
                            }
                        }
                    }

                    thisIsWorseAtEveryStat &= thisStat < thatStat;
                }
            }

            optimalReforges.put(thisReforgeStatModel, !thisIsWorseAtEveryStat);
        }

        // Build Item Entity
        return this.createItemEntity(reforgeTypeModel, objectData, optimalReforges
            .stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .map(reforgeStatModel -> {
                // Populate Default Effects
                ConcurrentMap<String, Double> effects = Concurrent.newMap(reforgeStatModel.getEffects());

                // Handle Bonus Reforge Effects
                SimplifiedApi.getRepositoryOf(BonusReforgeStatModel.class)
                    .findFirst(BonusReforgeStatModel::getReforge, reforgeStatModel.getReforge())
                    .ifPresent(bonusReforgeStatModel -> this.getImportantStats().forEach(statModel -> effects.put(
                        statModel.getKey(),
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            effects.getOrDefault(statModel.getKey(), 0.0),
                            objectData.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusReforgeStatModel
                        )
                    )));

                // Handle Bonus Item Effects
                objectData.getBonusItemStatModel()
                    .filter(BonusItemStatModel::isForReforges)
                    .ifPresent(bonusItemStatModel -> this.getImportantStats().forEach(statModel -> effects.put(
                        statModel.getKey(),
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            effects.getOrDefault(statModel.getKey(), 0.0),
                            objectData.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusItemStatModel
                        )
                    )));

                // Handle Bonus Pet Ability Effects
                this.getPlayerStats()
                    .getBonusPetAbilityStatModels()
                    .stream()
                    .filter(BonusPetAbilityStatModel::isPercentage)
                    .filter(BonusPetAbilityStatModel::noRequiredItem)
                    .filter(BonusPetAbilityStatModel::noRequiredMobType)
                    .forEach(bonusPetAbilityStatModel -> this.getImportantStats().forEach(statModel -> effects.put(
                        statModel.getKey(),
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            effects.getOrDefault(statModel.getKey(), 0.0),
                            null,
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusPetAbilityStatModel
                        )
                    )));

                return new ReforgeFact(reforgeStatModel, effects);
            })
            .collect(Concurrent.toList())
        );
    }

    public abstract SimpleBigDecimalScore getScore();

}
