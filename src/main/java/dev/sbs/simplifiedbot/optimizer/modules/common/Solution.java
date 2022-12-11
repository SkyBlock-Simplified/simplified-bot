package dev.sbs.simplifiedbot.optimizer.modules.common;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.AccessoryData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ItemData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ObjectData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.PlayerDataHelper;
import dev.sbs.api.data.model.skyblock.bonus_data.bonus_item_stats.BonusItemStatModel;
import dev.sbs.api.data.model.skyblock.bonus_data.bonus_pet_ability_stats.BonusPetAbilityStatModel;
import dev.sbs.api.data.model.skyblock.bonus_data.bonus_reforge_stats.BonusReforgeStatModel;
import dev.sbs.api.data.model.skyblock.item_types.ItemTypeModel;
import dev.sbs.api.data.model.skyblock.reforge_data.reforge_stats.ReforgeStatModel;
import dev.sbs.api.data.model.skyblock.stats.StatModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.data.mutable.MutableDouble;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.ListUtil;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class Solution<T extends ItemEntity> {

    @Getter private final ConcurrentList<ReforgeStatModel> allReforgeStatModels = SimplifiedApi.getRepositoryOf(ReforgeStatModel.class).findAll();
    @Getter private final Map<String, Double> computedStats = new ConcurrentMap<>();
    @Getter private PlayerStats playerStats;
    @Getter private OptimizerRequest optimizerRequest;
    @Getter private ConcurrentList<StatModel> importantStats;

    protected Solution() {

    } // Optimizer Cloning

    protected Solution(@NotNull OptimizerRequest optimizerRequest, @NotNull ConcurrentList<StatModel> importantStats) {
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
    }

    protected abstract @NotNull T createItemEntity(@NotNull ItemTypeModel reforgeTypeModel, @NotNull ObjectData<?> objectData, @NotNull ConcurrentList<ReforgeFact> optimalReforges);

    /**
     * Finds the applicable {@link ReforgeFact}s for each {@link ItemEntity}.
     *
     * @return {@link Pair} holding lists of all the {@link ItemEntity ItemEntities} and {@link ReforgeFact ReforgeFacts}.
     */
    protected @NotNull Pair<ConcurrentList<T>, ConcurrentList<ReforgeFact>> generateAvailableItems() {
        ConcurrentList<T> availableItems = Concurrent.newList();

        // Handle Accessories
        /*this.getOptimizerRequest()
            .getPlayerStats()
            .getAccessoryBag()
            .getFilteredAccessories()
            .forEach(accessoryData -> availableItems.add(this.getOptimalReforges(
                accessoryReforgeTypeModel,
                accessoryData
            )));*/

        // Handle Armor
        this.getOptimizerRequest()
            .getPlayerStats()
            .getArmor()
            .stream()
            .flatMap(Optional::stream)
            .forEach(itemData -> availableItems.add(this.getOptimalReforges(
                itemData.getItem().getType(),
                itemData
            )));

        // Handle Weapon
        this.getOptimizerRequest()
            .getWeapon()
            .ifPresent(weaponData -> availableItems.add(this.getOptimalReforges(
                weaponData.getItem().getType(),
                weaponData
            )));

        return Pair.of(availableItems, availableItems.stream()
            .flatMap(itemEntity -> itemEntity.getAvailableReforges().stream())
            .collect(Concurrent.toList())
        );
    }

    public abstract @NotNull List<ReforgeFact> getAllReforges();

    public abstract @NotNull List<T> getAvailableItems();

    private static double getConstSum(@NotNull PlayerStats playerStats, @NotNull StatModel statModel) {
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
            .mapToDouble(armorData -> getDataSum(armorData, statModel, ItemData.Type.values()))
            .sum();

        // Accessory Stats
        total += playerStats.getAccessoryBag()
            .getFilteredAccessories()
            .stream()
            .mapToDouble(accessoryData -> getDataSum(accessoryData, statModel, AccessoryData.Type.values()))
            .sum();

        return total;
    }

    private static double getConstSum(@NotNull OptimizerRequest.WeaponData weaponData, @NotNull StatModel statModel) {
        return getDataSum(weaponData, statModel, ItemData.Type.values());
    }

    private static <T extends ObjectData.Type> double getDataSum(@NotNull ObjectData<T> objectData, @NotNull StatModel statModel, @NotNull T[] values) {
        return Arrays.stream(values)
            .filter(ObjectData.Type::isOptimizerConstant)
            .mapToDouble(type -> objectData.getData(statModel, type).getTotal())
            .sum();
    }

    private T getOptimalReforges(@NotNull ItemTypeModel itemTypeModel, @NotNull ObjectData<?> objectData) {
        // Filter by Rarity and Allowed Reforge Stones
        ConcurrentMap<ReforgeStatModel, Boolean> optimalReforges = this.getAllReforgeStatModels()
            .stream()
            .filter(reforgeStatModel -> reforgeStatModel.getReforge().getItemTypes().contains(itemTypeModel.getKey()))
            .filter(reforgeStatModel -> reforgeStatModel.getRarity().equals(objectData.getRarity()))
            .filter(reforgeStatModel -> this.getOptimizerRequest().getAllowedReforges().contains(reforgeStatModel))
            .filter(reforgeStatModel -> ListUtil.isEmpty(reforgeStatModel.getReforge().getConditions()) ||
                reforgeStatModel.getReforge()
                    .getConditions()
                    .stream()
                    .anyMatch(reforgeConditionModel -> reforgeConditionModel.getItem().equals(objectData.getItem()))
            )
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
                    if (ListUtil.notEmpty(objectData.getBonusItemStatModels())) {
                        for (BonusItemStatModel bonusItemStatModel : objectData.getBonusItemStatModels()) {
                            if (bonusItemStatModel.noRequiredMobType() || this.getOptimizerRequest().getMobType().equals(bonusItemStatModel.getRequiredMobType())) {
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
        return this.createItemEntity(itemTypeModel, objectData, optimalReforges
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
                objectData.getBonusItemStatModels()
                    .stream()
                    .filter(BonusItemStatModel::isForReforges)
                    .filter(bonusItemStatModel -> bonusItemStatModel.noRequiredMobType() || optimizerRequest.getMobType().equals(bonusItemStatModel.getRequiredMobType()))
                    .forEach(bonusItemStatModel -> this.getImportantStats().forEach(statModel -> effects.put(
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

    public abstract @NotNull SimpleBigDecimalScore getScore();

}
