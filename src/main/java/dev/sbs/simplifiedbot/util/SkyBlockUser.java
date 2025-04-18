package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.impl.hypixel.request.HypixelRequest;
import dev.sbs.api.client.impl.hypixel.response.hypixel.implementation.HypixelGuild;
import dev.sbs.api.client.impl.hypixel.response.hypixel.implementation.HypixelSession;
import dev.sbs.api.client.impl.hypixel.response.skyblock.SkyBlockProfilesResponse;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.SkyBlockAuction;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.SkyBlockIsland;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.member.Member;
import dev.sbs.api.client.impl.sbs.request.SbsRequest;
import dev.sbs.api.client.impl.sbs.response.MojangProfileResponse;
import dev.sbs.api.client.impl.sbs.response.SkyBlockEmojis;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.data.model.discord.users.UserModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.util.StringUtil;
import dev.sbs.discordapi.command.exception.input.InputException;
import dev.sbs.discordapi.command.parameter.Argument;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.exception.DiscordUserException;
import dev.sbs.simplifiedbot.SimplifiedBot;
import dev.sbs.simplifiedbot.exception.UnlinkedAccountException;
import discord4j.common.util.Snowflake;
import lombok.Getter;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public final class SkyBlockUser {

    private final SkyBlockProfilesResponse profiles;
    @Getter private final MojangProfileResponse mojangProfile;
    @Getter private SkyBlockIsland selectedIsland;
    @Getter private final SkyBlockEmojis skyBlockEmojis;
    @Getter private final ConcurrentList<SkyBlockAuction> auctions;
    @Getter private final ItemCache.AuctionHouse auctionHouse;
    @Getter private final Optional<HypixelGuild> guild;
    @Getter private final HypixelSession session;

    @PrintFormat
    public SkyBlockUser(@NotNull SlashCommandContext commandContext) {
        this.auctionHouse = ((SimplifiedBot) commandContext.getDiscordBot()).getItemCache().getAuctionHouse();
        this.skyBlockEmojis = ((SimplifiedBot) commandContext.getDiscordBot()).getSkyBlockEmojis();
        Optional<String> optionalPlayerID = commandContext.getArgument("name").map(Argument::asString);

        if (optionalPlayerID.isEmpty()) {
            if (!this.isVerified(commandContext.getInteractUserId()))
                throw new UnlinkedAccountException();

            optionalPlayerID = SimplifiedApi.getRepositoryOf(UserModel.class)
                .matchFirst(userModel -> userModel.getDiscordIds().contains(commandContext.getInteractUserId().asLong()))
                .map(userModel -> userModel.getMojangUniqueIds().get(userModel.getMojangUniqueIds().size() - 1))
                .map(UUID::toString);
        }

        String playerID = optionalPlayerID.orElseThrow(); // Will never reach here
        SbsRequest mojangRequest = SimplifiedApi.getApiRequest(SbsRequest.class);
        this.mojangProfile = StringUtil.isUUID(playerID) ? mojangRequest.getProfileFromUniqueId(StringUtil.toUUID(playerID)) : mojangRequest.getProfileFromUsername(playerID);
        this.profiles = SimplifiedApi.getApiRequest(HypixelRequest.class).getProfiles(this.getMojangProfile().getUniqueId());
        this.guild = SimplifiedApi.getApiRequest(HypixelRequest.class).getGuildByPlayer(this.getMojangProfile().getUniqueId()).getGuild();
        this.session = SimplifiedApi.getApiRequest(HypixelRequest.class).getStatus(this.getMojangProfile().getUniqueId()).getSession();
        this.auctions = SimplifiedApi.getApiRequest(HypixelRequest.class).getAuctionByPlayer(this.getMojangProfile().getUniqueId()).getAuctions();

        // Empty Profile
        if (this.profiles.getIslands().isEmpty())
            throw new DiscordUserException("The Hypixel account `%s` has either never played SkyBlock or has been profile wiped.", this.getMojangProfile().getUsername());

        Optional<String> optionalProfileName = commandContext.getArgument("profile").map(Argument::asString);
        Optional<SkyBlockIsland> optionalSkyBlockIsland = Optional.empty();

        if (optionalProfileName.isPresent()) {
            String profileName = optionalProfileName.get();
            ProfileModel profileModel = SimplifiedApi.getRepositoryOf(ProfileModel.class).findFirstOrNull(ProfileModel::getKey, profileName.toUpperCase());

            // Invalid Profile Name
            if (profileModel == null)
                throw new InputException(StringUtil.capitalizeFully(profileName));

            optionalSkyBlockIsland = this.profiles.getIsland(profileModel.getKey());
        }

        this.selectedIsland = optionalSkyBlockIsland.orElse(this.profiles.getSelected());
    }

    public @NotNull ConcurrentList<SkyBlockIsland> getIslands() {
        return this.profiles.getIslands();
    }

    public Optional<SkyBlockIsland> getIsland(@NotNull ProfileModel profileModel) {
        return this.profiles.getIsland(profileModel.getKey());
    }

    public @NotNull SkyBlockIsland getSelected() {
        return this.profiles.getSelected();
    }

    public @NotNull Member getMember() {
        return this.getMember(this.getMojangProfile().getUniqueId());
    }

    public Member getMember(@NotNull UUID uniqueId) {
        return this.selectedIsland.getMembers().get(uniqueId);
    }

    public boolean isVerified(@NotNull Snowflake userId) {
        return SimplifiedApi.getRepositoryOf(UserModel.class).matchFirst(userModel -> userModel.getDiscordIds().contains(userId.asLong())).isPresent();
    }

    public void setSelectedIsland(@NotNull ProfileModel profileModel) {
        this.selectedIsland = this.getIsland(profileModel).orElse(this.selectedIsland);
    }

}
