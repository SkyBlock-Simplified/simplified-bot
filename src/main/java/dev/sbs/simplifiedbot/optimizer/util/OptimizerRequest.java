package dev.sbs.simplifiedbot.optimizer.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.impl.hypixel.request.HypixelRequest;
import dev.sbs.api.client.impl.hypixel.response.skyblock.SkyBlockProfilesResponse;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.SkyBlockIsland;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.member.Member;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.profile_stats.ProfileStats;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.profile_stats.data.ItemData;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.profile_stats.data.PlayerDataHelper;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.util.NbtContent;
import dev.sbs.api.client.impl.sbs.request.SbsRequest;
import dev.sbs.api.client.impl.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.discord.optimizer_mob_types.OptimizerMobTypeModel;
import dev.sbs.api.data.model.skyblock.bonus_data.bonus_pet_ability_stats.BonusPetAbilityStatModel;
import dev.sbs.api.data.model.skyblock.items.ItemModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.data.model.skyblock.reforge_data.reforge_stats.ReforgeStatModel;
import dev.sbs.api.minecraft.nbt.tags.collection.CompoundTag;
import dev.sbs.api.minecraft.nbt.tags.primitive.StringTag;
import dev.sbs.api.util.builder.Builder;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public final class OptimizerRequest {

    private final Member member;
    private final ProfileStats profileStats;
    private final ConcurrentMap<String, Double> expressionVariables;
    private final Optional<WeaponData> weapon;
    private final ConcurrentList<ReforgeStatModel> allowedReforges;
    private final Type type;
    private final OptimizerMobTypeModel mobType;
    private final double playerDamage;
    private final double weaponDamage;
    private final double weaponBonus;

    private OptimizerRequest(OptimizerRequestBuilder optimizerRequestBuilder) {
        SkyBlockIsland island = optimizerRequestBuilder.skyBlockProfilesResponse.getIslands().get(optimizerRequestBuilder.islandIndex);
        Member member = island.getMembers().get(optimizerRequestBuilder.uniqueId);

        this.member = member;
        this.profileStats = island.getProfileStats(member);
        this.expressionVariables = this.profileStats.getExpressionVariables();
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
                    .map(NbtContent::getNbtData)
                    .flatMap(compoundTag -> compoundTag.<CompoundTag>getList("i").stream())
                    .filter(CompoundTag::notEmpty)
                    .filter(itemTag -> itemTag.getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY).getValue().equals(optimizerRequestBuilder.weaponItemModel.get().getItemId()))
                    .findFirst()
                    .map(itemTag -> new WeaponData(itemTag, this));
            }
        }

        this.weapon = optionalWeapon;
        this.playerDamage = this.getProfileStats().getCombinedStats().get(OptimizerHelper.DAMAGE_STAT_MODEL).getTotal();
        this.weaponDamage = OptimizerHelper.getWeaponDamage(this);
        this.weaponBonus = OptimizerHelper.getWeaponBonus(this);
    }

    public static OptimizerRequestBuilder of(String username) {
        MojangProfileResponse mojangProfileResponse = SimplifiedApi.getApiRequest(SbsRequest.class).getProfileFromUsername(username);
        SkyBlockProfilesResponse skyBlockProfilesResponse = SimplifiedApi.getApiRequest(HypixelRequest.class).getProfiles(mojangProfileResponse.getUniqueId());
        return new OptimizerRequestBuilder(skyBlockProfilesResponse, mojangProfileResponse.getUniqueId());
    }

    public static OptimizerRequestBuilder of(UUID uniqueId) {
        SkyBlockProfilesResponse skyBlockProfilesResponse = SimplifiedApi.getApiRequest(HypixelRequest.class).getProfiles(uniqueId);
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
            return this.withIsland(this.skyBlockProfilesResponse.getIsland(profileModel.getKey())
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
                compoundTag
            );
            this.optimizerRequest = optimizerRequest;

            // Add Pet Ability Stats
            this.getOptimizerRequest()
                .getProfileStats()
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
                .getProfileStats()
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