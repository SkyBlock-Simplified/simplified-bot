package dev.sbs.simplifiedbot.optimizer.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.SimplifiedException;
import dev.sbs.api.client.hypixel.implementation.HypixelSkyBlockData;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockProfilesResponse;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ItemData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.PlayerDataHelper;
import dev.sbs.api.client.mojang.implementation.MojangData;
import dev.sbs.api.client.mojang.response.MojangProfileResponse;
import dev.sbs.api.data.model.discord.optimizer_mob_types.OptimizerMobTypeModel;
import dev.sbs.api.data.model.skyblock.items.ItemModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.data.model.skyblock.reforge_stats.ReforgeStatModel;
import dev.sbs.api.minecraft.nbt.tags.collection.CompoundTag;
import dev.sbs.api.minecraft.nbt.tags.primitive.StringTag;
import dev.sbs.api.util.LogTime;
import dev.sbs.api.util.builder.Builder;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentList;
import dev.sbs.api.util.concurrent.ConcurrentMap;
import dev.sbs.simplifiedbot.optimizer.exception.OptimizerException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class OptimizerRequest {

    private static final ConcurrentList<SkyBlockIsland.Storage> weaponStorage = Concurrent.newList(SkyBlockIsland.Storage.INVENTORY, SkyBlockIsland.Storage.ENDER_CHEST);
    @Getter private final SkyBlockIsland.Member member;
    @Getter private final PlayerStats playerStats;
    @Getter private final ConcurrentMap<String, Double> expressionVariables;
    @Getter private final Optional<Weapon> weapon;
    @Getter private final ConcurrentList<ReforgeStatModel> allowedReforges;
    @Getter private final Type type;
    @Getter private final OptimizerMobTypeModel mobType;

    private OptimizerRequest(OptimizerRequestBuilder optimizerRequestBuilder) {
        SkyBlockIsland island = optimizerRequestBuilder.skyBlockProfilesResponse.getIslands().get(optimizerRequestBuilder.islandIndex);
        Optional<SkyBlockIsland.Member> optionalMember = island.getMember(optimizerRequestBuilder.uniqueId);
        SkyBlockIsland.Member member = optionalMember.orElseThrow(
            () -> SimplifiedException.of(OptimizerException.class)
                .withMessage("No member found!")
                .build()
        );

        this.member = member;
        LogTime.log("Start loading player stats");
        this.playerStats = island.getPlayerStats(member);
        LogTime.log("After loading player stats");
        this.expressionVariables = this.playerStats.getExpressionVariables();
        this.allowedReforges = optimizerRequestBuilder.allowedReforges;
        this.type = optimizerRequestBuilder.type;
        this.mobType = optimizerRequestBuilder.mobType;

        // Load Weapon
        Optional<Weapon> optionalWeapon = Optional.empty();
        if (optimizerRequestBuilder.weaponItemModel.isPresent()) {
            // Check Inventories
            for (SkyBlockIsland.Storage storage : weaponStorage) {
                optionalWeapon = member.getStorage(storage)
                    .getNbtData()
                    .<CompoundTag>getList("i")
                    .stream()
                    .filter(CompoundTag::notEmpty)
                    .filter(itemTag -> itemTag.getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY).getValue().equals(optimizerRequestBuilder.weaponItemModel.get().getItemId()))
                    .findFirst()
                    .map(itemTag -> new Weapon(itemTag, this.getExpressionVariables()));

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
                    .map(itemTag -> new Weapon(itemTag, this.getExpressionVariables()));
            }
        }
        this.weapon = optionalWeapon;
        LogTime.log("After loading weapon");
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
            return this.withIsland(this.skyBlockProfilesResponse.getIslands()
                .stream()
                .filter(skyBlockIsland -> skyBlockIsland.getProfileName().map(profileModel::equals).orElse(false))
                .findFirst()
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

    public static class Weapon {

        @Getter private final ItemData itemData;

        private Weapon(@NotNull CompoundTag compoundTag, ConcurrentMap<String, Double> expressionVariables) {
            ItemModel itemModel = SimplifiedApi.getRepositoryOf(ItemModel.class).findFirstOrNull(ItemModel::getItemId, compoundTag.<StringTag>getPath("tag.ExtraAttributes.id").getValue());

            this.itemData = PlayerDataHelper.parseItemData(
                itemModel,
                compoundTag,
                "SWORD"
            );

            this.itemData.calculateBonus(expressionVariables);
        }

    }

}