package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.implementation.HypixelPlayerData;
import dev.sbs.api.client.hypixel.implementation.HypixelSkyBlockData;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelStatusResponse;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockAuction;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockProfilesResponse;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.sbs.implementation.MojangData;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.client.sbs.response.SkyBlockEmojisResponse;
import dev.sbs.api.data.model.discord.users.UserModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.util.SimplifiedException;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.helper.ListUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.command.data.Argument;
import dev.sbs.discordapi.command.exception.user.UserInputException;
import dev.sbs.discordapi.command.exception.user.UserVerificationException;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.simplifiedbot.SimplifiedBot;
import discord4j.common.util.Snowflake;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public final class SkyBlockUser {

    private final SkyBlockProfilesResponse profiles;
    @Getter private final MojangProfileResponse mojangProfile;
    @Getter private SkyBlockIsland selectedIsland;
    @Getter private final SkyBlockEmojisResponse skyBlockEmojis;
    @Getter private final ConcurrentList<SkyBlockAuction> auctions;
    @Getter private final ItemCache.AuctionHouse auctionHouse;
    @Getter private final Optional<HypixelGuildResponse.Guild> guild;
    @Getter private final HypixelStatusResponse.Session session;

    public SkyBlockUser(CommandContext<?> commandContext) {
        this.auctionHouse = ((SimplifiedBot) commandContext.getDiscordBot()).getItemCache().getAuctionHouse();
        this.skyBlockEmojis = ((SimplifiedBot) commandContext.getDiscordBot()).getSkyBlockEmojis();
        Optional<String> optionalPlayerID = commandContext.getArgument("name").flatMap(Argument::getValue);

        if (optionalPlayerID.isEmpty()) {
            if (!this.isVerified(commandContext.getInteractUserId()))
                throw SimplifiedException.of(UserVerificationException.class)
                    .addData("COMMAND", true)
                    .build();

            optionalPlayerID = SimplifiedApi.getRepositoryOf(UserModel.class)
                .matchFirst(userModel -> userModel.getDiscordIds().contains(commandContext.getInteractUserId().asLong()))
                .map(userModel -> userModel.getMojangUniqueIds().get(userModel.getMojangUniqueIds().size() - 1))
                .map(UUID::toString);
        }

        String playerID = optionalPlayerID.orElseThrow(); // Will never reach here
        MojangData mojangData = SimplifiedApi.getWebApi(MojangData.class);
        this.mojangProfile = StringUtil.isUUID(playerID) ? mojangData.getProfileFromUniqueId(StringUtil.toUUID(playerID)) : mojangData.getProfileFromUsername(playerID);
        this.profiles = SimplifiedApi.getWebApi(HypixelSkyBlockData.class).getProfiles(this.getMojangProfile().getUniqueId());
        this.guild = SimplifiedApi.getWebApi(HypixelPlayerData.class).getGuildByPlayer(this.getMojangProfile().getUniqueId()).getGuild();
        this.session = SimplifiedApi.getWebApi(HypixelPlayerData.class).getStatus(this.getMojangProfile().getUniqueId()).getSession();
        this.auctions = SimplifiedApi.getWebApi(HypixelSkyBlockData.class).getAuctionByPlayer(this.getMojangProfile().getUniqueId()).getAuctions();

        // Empty Profile
        if (ListUtil.isEmpty(this.profiles.getIslands())) {
            throw SimplifiedException.of(UserInputException.class)
                .withMessage("The Hypixel account `{0}` has either never played SkyBlock or has been profile wiped.", this.getMojangProfile().getUsername())
                .build();
        }

        Optional<String> optionalProfileName = commandContext.getArgument("profile").flatMap(Argument::getValue);
        Optional<SkyBlockIsland> optionalSkyBlockIsland = Optional.empty();

        if (optionalProfileName.isPresent()) {
            String profileName = optionalProfileName.get();
            ProfileModel profileModel = SimplifiedApi.getRepositoryOf(ProfileModel.class).findFirstOrNull(ProfileModel::getKey, profileName.toUpperCase());

            // Invalid Profile Name
            if (profileModel == null) {
                throw SimplifiedException.of(UserInputException.class)
                    .withMessage("The Hypixel account `{0}` does not contain a profile with name `{1}`.", this.getMojangProfile().getUsername(), WordUtil.capitalizeFully(profileName))
                    .build();
            }

            optionalSkyBlockIsland = this.profiles.getIsland(profileModel);
        }

        this.selectedIsland = optionalSkyBlockIsland.orElse(this.profiles.getLastPlayed(this.getMojangProfile().getUniqueId()));
    }

    public ConcurrentList<SkyBlockIsland> getIslands() {
        return this.profiles.getIslands();
    }

    public Optional<SkyBlockIsland> getIsland(@NotNull ProfileModel profileModel) {
        return this.profiles.getIsland(profileModel);
    }

    public SkyBlockIsland getLastPlayed(UUID uniqueId) {
        return this.profiles.getLastPlayed(uniqueId);
    }

    public SkyBlockIsland.Member getMember() {
        return this.getMember(this.getMojangProfile().getUniqueId()).orElseThrow(); // Will never get here
    }

    public Optional<SkyBlockIsland.Member> getMember(UUID uniqueId) {
        return this.selectedIsland.getMember(uniqueId);
    }

    public boolean isVerified(Snowflake userId) {
        return SimplifiedApi.getRepositoryOf(UserModel.class).matchFirst(userModel -> userModel.getDiscordIds().contains(userId.asLong())).isPresent();
    }

    public void setSelectedIsland(@NotNull ProfileModel profileModel) {
        this.selectedIsland = this.getIsland(profileModel).orElse(this.selectedIsland);
    }

}
