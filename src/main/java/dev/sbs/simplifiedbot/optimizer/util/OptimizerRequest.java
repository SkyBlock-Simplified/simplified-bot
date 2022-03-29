package dev.sbs.simplifiedbot.optimizer.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.implementation.HypixelSkyBlockData;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockProfilesResponse;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ItemData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.PlayerDataHelper;
import dev.sbs.api.client.sbs.implementation.MojangData;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.discord.optimizer_mob_types.OptimizerMobTypeModel;
import dev.sbs.api.data.model.skyblock.bonus_pet_ability_stats.BonusPetAbilityStatModel;
import dev.sbs.api.data.model.skyblock.items.ItemModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.data.model.skyblock.reforge_stats.ReforgeStatModel;
import dev.sbs.api.minecraft.nbt.tags.collection.CompoundTag;
import dev.sbs.api.minecraft.nbt.tags.primitive.StringTag;
import dev.sbs.api.util.SimplifiedException;
import dev.sbs.api.util.builder.Builder;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.simplifiedbot.optimizer.exception.OptimizerException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class OptimizerRequest {

    private static final ConcurrentList<SkyBlockIsland.Storage> WEAPON_STORAGE = Concurrent.newList(SkyBlockIsland.Storage.INVENTORY, SkyBlockIsland.Storage.ENDER_CHEST);
    @Getter private final SkyBlockIsland.Member member;
    @Getter private final PlayerStats playerStats;
    @Getter private final ConcurrentMap<String, Double> expressionVariables;
    @Getter private final Optional<WeaponData> weapon;
    @Getter private final ConcurrentList<ReforgeStatModel> allowedReforges;
    @Getter private final Type type;
    @Getter private final OptimizerMobTypeModel mobType;
    @Getter private final double playerDamage;
    @Getter private final double weaponDamage;
    @Getter private final double petAbilityDamage;

    private OptimizerRequest(OptimizerRequestBuilder optimizerRequestBuilder) {
        SkyBlockIsland island = optimizerRequestBuilder.skyBlockProfilesResponse.getIslands().get(optimizerRequestBuilder.islandIndex);
        Optional<SkyBlockIsland.Member> optionalMember = island.getMember(optimizerRequestBuilder.uniqueId);
        SkyBlockIsland.Member member = optionalMember.orElseThrow(
            () -> SimplifiedException.of(OptimizerException.class)
                .withMessage("No member found!")
                .build()
        );

        this.member = member;
        this.playerStats = island.getPlayerStats(member);
        this.expressionVariables = this.playerStats.getExpressionVariables();
        this.allowedReforges = optimizerRequestBuilder.allowedReforges;
        this.type = optimizerRequestBuilder.type;
        this.mobType = optimizerRequestBuilder.mobType;

        // Load Weapon
        Optional<WeaponData> optionalWeapon = Optional.empty();

        if (optimizerRequestBuilder.weaponItemModel.isPresent()) {
            // Check Inventories
            for (SkyBlockIsland.Storage storage : WEAPON_STORAGE) {
                optionalWeapon = member.getStorage(storage)
                    .getNbtData()
                    .<CompoundTag>getList("i")
                    .stream()
                    .filter(CompoundTag::notEmpty)
                    .filter(itemTag -> itemTag.getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY).getValue().equals(optimizerRequestBuilder.weaponItemModel.get().getItemId()))
                    .findFirst()
                    .map(itemTag -> new WeaponData(itemTag, this));

                if (optionalWeapon.isPresent())
                    break;
            }

            // Check Backpacks
            if (optionalWeapon.isEmpty()) {
                optionalWeapon = member.getBackpacks()
                    .getContents()
                    .stream()
                    .map(Map.Entry::getValue)
                    .map(SkyBlockIsland.NbtContent::getNbtData)
                    .flatMap(compoundTag -> compoundTag.<CompoundTag>getList("i").stream())
                    .filter(CompoundTag::notEmpty)
                    .filter(itemTag -> itemTag.getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY).getValue().equals(optimizerRequestBuilder.weaponItemModel.get().getItemId()))
                    .findFirst()
                    .map(itemTag -> new WeaponData(itemTag, this));
            }
        }

        this.weapon = optionalWeapon;
        this.playerDamage = this.getPlayerStats().getCombinedStats().get(OptimizerHelper.DAMAGE_STAT_MODEL).getTotal();
        this.weaponDamage = OptimizerHelper.getWeaponDamage(this);
        this.petAbilityDamage = OptimizerHelper.getPetAbilityDamage(this);
    }

    public static OptimizerRequestBuilder of(String username) {
        MojangProfileResponse mojangProfileResponse = SimplifiedApi.getWebApi(MojangData.class).getProfileFromUsername(username);
        SkyBlockProfilesResponse skyBlockProfilesResponse = SimplifiedApi.getWebApi(HypixelSkyBlockData.class).getProfiles(mojangProfileResponse.getUniqueId());
        return new OptimizerRequestBuilder(skyBlockProfilesResponse, mojangProfileResponse.getUniqueId());
    }

    public static OptimizerRequestBuilder of(UUID uniqueId) {
        SkyBlockProfilesResponse skyBlockProfilesResponse = SimplifiedApi.getWebApi(HypixelSkyBlockData.class).getProfiles(uniqueId);
        return new OptimizerRequestBuilder(skyBlockProfilesResponse, uniqueId);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OptimizerRequestBuilder implements Builder<OptimizerRequest> {

        private final SkyBlockProfilesResponse skyBlockProfilesResponse;
        private final UUID uniqueId;
        private final ConcurrentList<ReforgeStatModel> allowedReforges = Concurrent.newList();
        private Type type;
        private OptimizerMobTypeModel mobType;
        private int islandIndex;
        private Optional<ItemModel> weaponItemModel;

        public OptimizerRequestBuilder withAllowedReforges(ConcurrentList<ReforgeStatModel> allowedReforges) {
            this.allowedReforges.addAll(allowedReforges);
            return this;
        }

        public OptimizerRequestBuilder withIsland(@NotNull ProfileModel profileModel) {
            return this.withIsland(this.skyBlockProfilesResponse.getIsland(profileModel)
                .map(skyBlockIsland -> skyBlockProfilesResponse.getIslands().indexOf(skyBlockIsland))
                .orElse(0)
            );
        }

        public OptimizerRequestBuilder withIsland(int islandIndex) {
            this.islandIndex = islandIndex;
            return this;
        }

        public OptimizerRequestBuilder withMobType(OptimizerMobTypeModel mobType) {
            this.mobType = mobType;
            return this;
        }

        public OptimizerRequestBuilder withType(Type type) {
            this.type = type;
            return this;
        }

        public OptimizerRequestBuilder withWeapon(ItemModel itemModel) {
            this.weaponItemModel = Optional.ofNullable(itemModel);
            return this;
        }

        @Override
        public OptimizerRequest build() {
            return new OptimizerRequest(this);
        }

    }

    public enum Type {

        DAMAGE_PER_HIT,
        DAMAGE_PER_SECOND

    }

    public static class WeaponData extends ItemData {

        @Getter(AccessLevel.PROTECTED)
        private final OptimizerRequest optimizerRequest;

        private WeaponData(@NotNull CompoundTag compoundTag, OptimizerRequest optimizerRequest) {
            super(
                SimplifiedApi.getRepositoryOf(ItemModel.class).findFirstOrNull(
                    ItemModel::getItemId,
                    compoundTag.<StringTag>getPath("tag.ExtraAttributes.id").getValue()
                ),
                compoundTag,
                "SWORD"
            );
            this.optimizerRequest = optimizerRequest;

            // Add Pet Ability Stats
            this.getOptimizerRequest()
                .getPlayerStats()
                .getBonusPetAbilityStatModels()
                .stream()
                .filter(BonusPetAbilityStatModel::notPercentage)
                .filter(BonusPetAbilityStatModel::hasRequiredItem)
                .filter(bonusPetAbilityStatModel -> this.getItem().equals(bonusPetAbilityStatModel.getRequiredItem()))
                .filter(bonusPetAbilityStatModel -> bonusPetAbilityStatModel.noRequiredMobType() || bonusPetAbilityStatModel.getRequiredMobType().equals(optimizerRequest.getMobType()))
                .forEach(bonusPetAbilityStatModel -> this.getStats(Type.STATS)
                    .forEach((statModel, statData) -> this.setBonus(
                        statData,
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            statData.getBonus(),
                            this.getCompoundTag(),
                            optimizerRequest.getExpressionVariables(),
                            bonusPetAbilityStatModel
                        ))
                    )
                );

            this.calculateBonus(this.getOptimizerRequest().getExpressionVariables());
        }


        @Override
        public WeaponData calculateBonus(ConcurrentMap<String, Double> expressionVariables) {
            super.calculateBonus(expressionVariables);

            this.getOptimizerRequest()
                .getPlayerStats()
                .getBonusPetAbilityStatModels()
                .stream()
                .filter(BonusPetAbilityStatModel::isPercentage)
                .filter(BonusPetAbilityStatModel::noRequiredItem)
                .filter(BonusPetAbilityStatModel::noRequiredMobType)
                .forEach(bonusPetAbilityStatModel -> this.getStats().forEach((type, statEntries) -> statEntries.forEach((statModel, statData) -> {
                    this.setBase(
                        statData,
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            statData.getBase(),
                            this.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusPetAbilityStatModel
                        )
                    );

                    this.setBonus(
                        statData,
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            statData.getBonus(),
                            this.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusPetAbilityStatModel
                        )
                    );
                })));

            return this;
        }

    }

}