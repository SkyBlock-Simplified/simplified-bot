package dev.sbs.simplifiedbot.optimizer.modules.common;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.api.tuple.pair.Pair;
import dev.sbs.api.util.mutable.MutableDouble;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.model.BonusItemStat;
import dev.sbs.minecraftapi.model.BonusPetAbilityStat;
import dev.sbs.minecraftapi.model.BonusReforgeStat;
import dev.sbs.minecraftapi.model.ItemCategory;
import dev.sbs.minecraftapi.model.Reforge;
import dev.sbs.minecraftapi.model.Stat;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import dev.sbs.simplifiedbot.profile_stats.ProfileStats;
import dev.sbs.simplifiedbot.profile_stats.data.AccessoryData;
import dev.sbs.simplifiedbot.profile_stats.data.ItemData;
import dev.sbs.simplifiedbot.profile_stats.data.ObjectData;
import dev.sbs.simplifiedbot.profile_stats.data.PlayerDataHelper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public abstract class Solution<T extends ItemEntity> {

    private final ConcurrentList<Reforge> allReforges = MinecraftApi.getRepository(Reforge.class).findAll();
    private final Map<String, Double> computedStats = new ConcurrentMap<>();
    private ProfileStats profileStats;
    private OptimizerRequest optimizerRequest;
    private ConcurrentList<Stat> importantStats;

    protected Solution() {

    } // Optimizer Cloning

    protected Solution(@NotNull OptimizerRequest optimizerRequest, @NotNull ConcurrentList<Stat> importantStats) {
        this.optimizerRequest = optimizerRequest;
        this.importantStats = importantStats;
        this.profileStats = optimizerRequest.getProfileStats();

        // Handle Constant Stats
        this.getImportantStats().forEach(statModel -> {
            MutableDouble value = new MutableDouble();
            value.add(getConstSum(this.getProfileStats(), statModel)); // Add Player, Accessory and Item Stats
            this.getOptimizerRequest().getWeapon().ifPresent(weaponData -> value.add(getConstSum(weaponData, statModel))); // Add Weapon Stats
            this.computedStats.put(statModel.getId(), value.get());
        });
    }

    protected abstract @NotNull T createItemEntity(@NotNull ItemCategory itemCategory, @NotNull ObjectData<?> objectData, @NotNull ConcurrentList<ReforgeFact> optimalReforges);

    /**
     * Finds the applicable {@link ReforgeFact}s for each {@link ItemEntity}.
     *
     * @return {@link Pair} holding lists of all the {@link ItemEntity ItemEntities} and {@link ReforgeFact ReforgeFacts}.
     */
    protected @NotNull Pair<ConcurrentList<T>, ConcurrentList<ReforgeFact>> generateAvailableItems() {
        ConcurrentList<T> availableItems = Concurrent.newList();

        // Handle Armor
        this.getOptimizerRequest()
            .getProfileStats()
            .getArmor()
            .stream()
            .flatMap(Optional::stream)
            .forEach(itemData -> availableItems.add(this.getOptimalReforges(
                itemData.getItem().getCategory(),
                itemData
            )));

        // Handle Weapon
        this.getOptimizerRequest()
            .getWeapon()
            .ifPresent(weaponData -> availableItems.add(this.getOptimalReforges(
                weaponData.getItem().getCategory(),
                weaponData
            )));

        return Pair.of(availableItems, availableItems.stream()
            .flatMap(itemEntity -> itemEntity.getAvailableReforges().stream())
            .collect(Concurrent.toList())
        );
    }

    public abstract @NotNull List<ReforgeFact> getAllReforgesFacts();

    public abstract @NotNull List<T> getAvailableItems();

    private static double getConstSum(@NotNull ProfileStats profileStats, @NotNull Stat statModel) {
        double total = 0.0;

        // Player Stats
        total += Arrays.stream(ProfileStats.Type.values())
            .filter(ProfileStats.Type::isOptimizerConstant)
            .mapToDouble(type -> profileStats.getStatsOf(type).get(statModel).getTotal())
            .sum();

        // Armor Stats
        total += profileStats.getArmor()
            .stream()
            .flatMap(Optional::stream)
            .mapToDouble(armorData -> getDataSum(armorData, statModel, ItemData.Type.values()))
            .sum();

        // Accessory Stats
        total += profileStats.getAccessoryBag()
            .getFilteredAccessories()
            .stream()
            .mapToDouble(accessoryData -> getDataSum(accessoryData, statModel, AccessoryData.Type.values()))
            .sum();

        return total;
    }

    private static double getConstSum(@NotNull OptimizerRequest.WeaponData weaponData, @NotNull Stat statModel) {
        return getDataSum(weaponData, statModel, ItemData.Type.values());
    }

    private static <T extends ObjectData.Type> double getDataSum(@NotNull ObjectData<T> objectData, @NotNull Stat statModel, @NotNull T[] values) {
        return Arrays.stream(values)
            .filter(ObjectData.Type::isOptimizerConstant)
            .mapToDouble(type -> objectData.getData(statModel, type).getTotal())
            .sum();
    }

    private T getOptimalReforges(@NotNull ItemCategory itemCategory, @NotNull ObjectData<?> objectData) {
        // Filter by Category, Rarity, and Allowed Reforge Stones
        ConcurrentMap<Reforge, Boolean> optimalReforges = this.getAllReforges()
            .stream()
            .filter(reforge -> reforge.getCategoryIds().contains(itemCategory.getId()))
            .filter(reforge -> reforge.getStats(objectData.getRarity()).notEmpty())
            .filter(reforge -> this.getOptimizerRequest().getAllowedReforges().contains(reforge))
            .filter(reforge -> reforge.getItemIds().isEmpty() || reforge.getItemIds().contains(objectData.getItem().getId()))
            .map(reforge -> Pair.of(reforge, false))
            .collect(Concurrent.toMap());

        // Compare Reforges (dominance elimination)
        for (Reforge thisReforge : optimalReforges.keySet()) {
            boolean thisIsWorseAtEveryStat = true;
            Optional<BonusReforgeStat> optionalThisBonusReforgeStat = MinecraftApi.getRepository(BonusReforgeStat.class)
                .findFirst(BonusReforgeStat::getReforgeId, thisReforge.getId());

            // Build this reforge's effects
            ConcurrentMap<String, Double> thisEffects = buildReforgeEffects(thisReforge, objectData.getRarity());

            for (Reforge thatReforge : optimalReforges.keySet()) {
                Optional<BonusReforgeStat> optionalThatBonusReforgeStat = MinecraftApi.getRepository(BonusReforgeStat.class)
                    .findFirst(BonusReforgeStat::getReforgeId, thatReforge.getId());

                ConcurrentMap<String, Double> thatEffects = buildReforgeEffects(thatReforge, objectData.getRarity());

                for (Stat statModel : this.getImportantStats()) {
                    double thisStat = thisEffects.getOrDefault(statModel.getId(), 0.0);
                    double thatStat = thatEffects.getOrDefault(statModel.getId(), 0.0);

                    // Handle Bonus Reforge Effects
                    if (optionalThisBonusReforgeStat.isPresent()) {
                        thisStat = PlayerDataHelper.handleBonusEffects(
                            statModel, thisStat, objectData.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            optionalThisBonusReforgeStat.get()
                        );
                    }

                    if (optionalThatBonusReforgeStat.isPresent()) {
                        thatStat = PlayerDataHelper.handleBonusEffects(
                            statModel, thatStat, objectData.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            optionalThatBonusReforgeStat.get()
                        );
                    }

                    // Handle Bonus Item Effects
                    if (objectData.getBonusItemStatModels().notEmpty()) {
                        for (BonusItemStat bonusItemStat : objectData.getBonusItemStatModels()) {
                            if (bonusItemStat.noRequiredMobType() || this.getOptimizerRequest().getMobType().getKey().equals(bonusItemStat.getRequiredMobTypeKey())) {
                                if (bonusItemStat.isForReforges()) {
                                    thisStat = PlayerDataHelper.handleBonusEffects(
                                        statModel, thisStat, objectData.getCompoundTag(),
                                        this.getOptimizerRequest().getExpressionVariables(), bonusItemStat
                                    );
                                    thatStat = PlayerDataHelper.handleBonusEffects(
                                        statModel, thatStat, objectData.getCompoundTag(),
                                        this.getOptimizerRequest().getExpressionVariables(), bonusItemStat
                                    );
                                }
                            }
                        }
                    }

                    // Handle Bonus Pet Ability Effects
                    for (BonusPetAbilityStat bonusPetAbilityStat : this.getProfileStats().getBonusPetAbilityStatModels()) {
                        if (bonusPetAbilityStat.isPercentage()) {
                            if (bonusPetAbilityStat.noRequiredItem() && bonusPetAbilityStat.noRequiredMobType()) {
                                thisStat = PlayerDataHelper.handleBonusEffects(
                                    statModel, thisStat, objectData.getCompoundTag(),
                                    this.getOptimizerRequest().getExpressionVariables(), bonusPetAbilityStat
                                );
                                thatStat = PlayerDataHelper.handleBonusEffects(
                                    statModel, thatStat, objectData.getCompoundTag(),
                                    this.getOptimizerRequest().getExpressionVariables(), bonusPetAbilityStat
                                );
                            }
                        }
                    }

                    thisIsWorseAtEveryStat &= thisStat < thatStat;
                }
            }

            optimalReforges.put(thisReforge, !thisIsWorseAtEveryStat);
        }

        // Build Item Entity
        return this.createItemEntity(itemCategory, objectData, optimalReforges
            .stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .map(reforge -> {
                // Populate Default Effects
                ConcurrentMap<String, Double> effects = buildReforgeEffects(reforge, objectData.getRarity());

                // Handle Bonus Reforge Effects
                MinecraftApi.getRepository(BonusReforgeStat.class)
                    .findFirst(BonusReforgeStat::getReforgeId, reforge.getId())
                    .ifPresent(bonusReforgeStat -> this.getImportantStats().forEach(statModel -> effects.put(
                        statModel.getId(),
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            effects.getOrDefault(statModel.getId(), 0.0),
                            objectData.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusReforgeStat
                        )
                    )));

                // Handle Bonus Item Effects
                objectData.getBonusItemStatModels()
                    .stream()
                    .filter(BonusItemStat::isForReforges)
                    .filter(bonusItemStat -> bonusItemStat.noRequiredMobType() || optimizerRequest.getMobType().getKey().equals(bonusItemStat.getRequiredMobTypeKey()))
                    .forEach(bonusItemStat -> this.getImportantStats().forEach(statModel -> effects.put(
                        statModel.getId(),
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            effects.getOrDefault(statModel.getId(), 0.0),
                            objectData.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusItemStat
                        )
                    )));

                // Handle Bonus Pet Ability Effects
                this.getProfileStats()
                    .getBonusPetAbilityStatModels()
                    .stream()
                    .filter(BonusPetAbilityStat::isPercentage)
                    .filter(BonusPetAbilityStat::noRequiredItem)
                    .filter(BonusPetAbilityStat::noRequiredMobType)
                    .forEach(bonusPetAbilityStat -> this.getImportantStats().forEach(statModel -> effects.put(
                        statModel.getId(),
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            effects.getOrDefault(statModel.getId(), 0.0),
                            null,
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusPetAbilityStat
                        )
                    )));

                return new ReforgeFact(reforge, objectData.getRarity(), effects);
            })
            .collect(Concurrent.toList())
        );
    }

    private static ConcurrentMap<String, Double> buildReforgeEffects(@NotNull Reforge reforge, @NotNull dev.sbs.minecraftapi.skyblock.common.Rarity rarity) {
        return reforge.getStats(rarity)
            .stream()
            .collect(Concurrent.toMap(Reforge.Substitute::getId, sub -> sub.getValues().get(rarity)));
    }

    public abstract @NotNull SimpleBigDecimalScore getScore();

}
