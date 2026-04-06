package dev.sbs.simplifiedbot.optimizer.util;

import dev.sbs.api.SimplifiedApi;
import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.ConcurrentMap;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.client.hypixel.HypixelClient;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.SkyBlockIsland;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.SkyBlockMember;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.SkyBlockProfiles;
import dev.sbs.minecraftapi.client.mojang.response.MojangProfile;
import dev.sbs.minecraftapi.client.sbs.SbsClient;
import dev.sbs.minecraftapi.nbt.tags.collection.CompoundTag;
import dev.sbs.minecraftapi.nbt.tags.primitive.StringTag;
import dev.sbs.minecraftapi.persistence.model.BonusPetAbilityStat;
import dev.sbs.minecraftapi.persistence.model.Item;
import dev.sbs.minecraftapi.persistence.model.Reforge;
import dev.sbs.minecraftapi.skyblock.common.NbtContent;
import dev.sbs.simplifiedbot.persistence.model.OptimizerMobType;
import dev.sbs.simplifiedbot.profile_stats.ProfileStats;
import dev.sbs.simplifiedbot.profile_stats.data.ItemData;
import dev.sbs.simplifiedbot.profile_stats.data.PlayerDataHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public final class OptimizerRequest {

    private final @NotNull SkyBlockMember member;
    private final @NotNull ProfileStats profileStats;
    private final @NotNull ConcurrentMap<String, Double> expressionVariables;
    private final @NotNull Optional<WeaponData> weapon;
    private final @NotNull ConcurrentList<Reforge> allowedReforges;
    private final @NotNull Type type;
    private final @NotNull OptimizerMobType mobType;
    private final double playerDamage;
    private final double weaponDamage;
    private final double weaponBonus;

    private OptimizerRequest(OptimizerRequestBuilder optimizerRequestBuilder) {
        SkyBlockIsland island = optimizerRequestBuilder.skyBlockProfilesResponse.getIslands().get(optimizerRequestBuilder.islandIndex);
        SkyBlockMember member = island.getMembers().get(optimizerRequestBuilder.uniqueId);

        this.member = member;
        this.profileStats = new ProfileStats(island, member, true);
        this.expressionVariables = this.profileStats.getExpressionVariables();
        this.allowedReforges = optimizerRequestBuilder.allowedReforges;
        this.type = optimizerRequestBuilder.type;
        this.mobType = optimizerRequestBuilder.mobType;

        // Load Weapon
        Optional<WeaponData> optionalWeapon = Optional.empty();

        if (optimizerRequestBuilder.weaponItem.isPresent()) {
            String weaponItemId = optimizerRequestBuilder.weaponItem.get().getId();

            // Inventory
            optionalWeapon = member.getInventory()
                .getContent()
                .getNbtData()
                .<CompoundTag>getListTag("i")
                .stream()
                .filter(CompoundTag::notEmpty)
                .filter(itemTag -> itemTag.getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY).getValue().equals(weaponItemId))
                .findFirst()
                .map(itemTag -> new WeaponData(itemTag, this));

            // Ender Chest
            if (optionalWeapon.isEmpty()) {
                optionalWeapon = member.getInventory()
                    .getEnderChest()
                    .getNbtData()
                    .<CompoundTag>getListTag("i")
                    .stream()
                    .filter(CompoundTag::notEmpty)
                    .filter(itemTag -> itemTag.getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY).getValue().equals(weaponItemId))
                    .findFirst()
                    .map(itemTag -> new WeaponData(itemTag, this));
            }

            // Backpacks
            if (optionalWeapon.isEmpty()) {
                optionalWeapon = member.getInventory()
                    .getBackpacks()
                    .stream()
                    .map(Map.Entry::getValue)
                    .map(NbtContent::getNbtData)
                    .flatMap(compoundTag -> compoundTag.<CompoundTag>getListTag("i").stream())
                    .filter(CompoundTag::notEmpty)
                    .filter(itemTag -> itemTag.getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY).getValue().equals(weaponItemId))
                    .findFirst()
                    .map(itemTag -> new WeaponData(itemTag, this));
            }
        }

        this.weapon = optionalWeapon;
        this.playerDamage = this.getProfileStats().getCombinedStats().get(OptimizerHelper.DAMAGE_STAT).getTotal();
        this.weaponDamage = OptimizerHelper.getWeaponDamage(this);
        this.weaponBonus = OptimizerHelper.getWeaponBonus(this);
    }

    public static OptimizerRequestBuilder of(String username) {
        MojangProfile mojangProfile = SimplifiedApi.getClient(SbsClient.class).getEndpoint().getProfileFromUsername(username);
        SkyBlockProfiles skyBlockProfilesResponse = SimplifiedApi.getClient(HypixelClient.class).getEndpoint().getProfiles(mojangProfile.getUniqueId());
        return new OptimizerRequestBuilder(skyBlockProfilesResponse, mojangProfile.getUniqueId());
    }

    public static OptimizerRequestBuilder of(UUID uniqueId) {
        SkyBlockProfiles skyBlockProfilesResponse = SimplifiedApi.getClient(HypixelClient.class).getEndpoint().getProfiles(uniqueId);
        return new OptimizerRequestBuilder(skyBlockProfilesResponse, uniqueId);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OptimizerRequestBuilder {

        private final SkyBlockProfiles skyBlockProfilesResponse;
        private final UUID uniqueId;
        private final ConcurrentList<Reforge> allowedReforges = Concurrent.newList();
        private Type type;
        private OptimizerMobType mobType;
        private int islandIndex;
        private Optional<Item> weaponItem = Optional.empty();

        public @NotNull OptimizerRequestBuilder withAllowedReforges(@NotNull ConcurrentList<Reforge> allowedReforges) {
            this.allowedReforges.addAll(allowedReforges);
            return this;
        }

        public @NotNull OptimizerRequestBuilder withIsland(int islandIndex) {
            this.islandIndex = islandIndex;
            return this;
        }

        public @NotNull OptimizerRequestBuilder withMobType(@NotNull OptimizerMobType mobType) {
            this.mobType = mobType;
            return this;
        }

        public @NotNull OptimizerRequestBuilder withType(@NotNull Type type) {
            this.type = type;
            return this;
        }

        public @NotNull OptimizerRequestBuilder withWeapon(@Nullable Item item) {
            this.weaponItem = Optional.ofNullable(item);
            return this;
        }

        public @NotNull OptimizerRequest build() {
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
                MinecraftApi.getRepository(Item.class).findFirstOrNull(
                    Item::getId,
                    compoundTag.getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY).getValue()
                ),
                compoundTag
            );
            this.optimizerRequest = optimizerRequest;

            // Add Pet Ability Stats
            this.getOptimizerRequest()
                .getProfileStats()
                .getBonusPetAbilityStatModels()
                .stream()
                .filter(BonusPetAbilityStat::notPercentage)
                .filter(BonusPetAbilityStat::hasRequiredItem)
                .filter(bonusPetAbilityStat -> this.getItem().equals(bonusPetAbilityStat.getRequiredItem()))
                .filter(bonusPetAbilityStat -> bonusPetAbilityStat.noRequiredMobType() || bonusPetAbilityStat.getRequiredMobTypeKey().equals(optimizerRequest.getMobType().getKey()))
                .forEach(bonusPetAbilityStat -> this.getStats(Type.STATS)
                    .forEach((statModel, statData) -> this.setBonus(
                        statData,
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            statData.getBonus(),
                            this.getCompoundTag(),
                            optimizerRequest.getExpressionVariables(),
                            bonusPetAbilityStat
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
                .filter(BonusPetAbilityStat::isPercentage)
                .filter(BonusPetAbilityStat::noRequiredItem)
                .filter(BonusPetAbilityStat::noRequiredMobType)
                .forEach(bonusPetAbilityStat -> this.getStats().forEach((type, statEntries) -> statEntries.forEach((statModel, statData) -> {
                    this.setBase(
                        statData,
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            statData.getBase(),
                            this.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusPetAbilityStat
                        )
                    );

                    this.setBonus(
                        statData,
                        PlayerDataHelper.handleBonusEffects(
                            statModel,
                            statData.getBonus(),
                            this.getCompoundTag(),
                            this.getOptimizerRequest().getExpressionVariables(),
                            bonusPetAbilityStat
                        )
                    );
                })));

            return this;
        }

    }

}
