package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.SimplifiedException;
import dev.sbs.api.client.hypixel.implementation.HypixelSkyBlockData;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockProfilesResponse;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.AccessoryData;
import dev.sbs.api.client.mojang.implementation.MojangData;
import dev.sbs.api.client.mojang.response.MojangProfileResponse;
import dev.sbs.api.data.model.discord.users.UserModel;
import dev.sbs.api.data.model.skyblock.accessories.AccessoryModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentList;
import dev.sbs.api.util.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.ListUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.api.util.search.function.SearchFunction;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.Argument;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.command.exception.user.UserInputException;
import dev.sbs.discordapi.command.exception.user.UserVerificationException;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.action.SelectMenu;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.PageItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@CommandInfo(
    id = "b0e6bdee-971c-4774-9373-a8ef3ccd4e5b",
    name = "missing"
)
public class MissingCommand extends Command {

    protected MissingCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {
        Optional<String> optionalPlayerID = commandContext.getArgument("name").flatMap(Argument::getValue);

        if (optionalPlayerID.isEmpty()) {
            if (!this.isUserVerified(commandContext.getInteractUserId()))
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
        MojangProfileResponse mojangProfileResponse = StringUtil.isUUID(playerID) ? mojangData.getProfileFromUniqueId(StringUtil.toUUID(playerID)) : mojangData.getProfileFromUsername(playerID);
        SkyBlockProfilesResponse skyBlockProfilesResponse = SimplifiedApi.getWebApi(HypixelSkyBlockData.class).getProfiles(mojangProfileResponse.getUniqueId());

        // Empty Profile
        if (ListUtil.isEmpty(skyBlockProfilesResponse.getIslands())) {
            throw SimplifiedException.of(UserInputException.class)
                .withMessage("The Hypixel account `{0}` has either never played SkyBlock or has been profile wiped.", mojangProfileResponse.getUsername())
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
                    .withMessage("The Hypixel account `{0}` does not contain a profile with name `{1}`.", mojangProfileResponse.getUsername(), WordUtil.capitalizeFully(profileName))
                    .build();
            }

            optionalSkyBlockIsland = skyBlockProfilesResponse.getIsland(profileModel);
        }

        SkyBlockIsland skyBlockIsland = optionalSkyBlockIsland.orElse(skyBlockProfilesResponse.getLastPlayed(mojangProfileResponse.getUniqueId()));
        skyBlockIsland.getMember(mojangProfileResponse.getUniqueId()).ifPresent(skyBlockMember -> {
            PlayerStats playerStats = skyBlockIsland.getPlayerStats(skyBlockMember);
            ConcurrentList<AccessoryModel> allAccessories = SimplifiedApi.getRepositoryOf(AccessoryModel.class).findAll();

            // Load Missing Accessories
            ConcurrentList<AccessoryModel> missingAccessories = allAccessories.stream()
                .filter(AccessoryModel::isAttainable)
                .filter(accessoryModel -> playerStats.getAccessories()
                    .stream()
                    .noneMatch(accessoryData -> accessoryData.getAccessory().equals(accessoryModel))
                )
                .filter(accessoryModel -> {
                    if (Objects.isNull(accessoryModel.getFamily()))
                        return true;

                    return playerStats.getAccessories()
                        .stream()
                        .map(AccessoryData::getAccessory)
                        .filter(playerAccessoryModel -> Objects.nonNull(playerAccessoryModel.getFamily()))
                        .filter(playerAccessoryModel -> playerAccessoryModel.getFamily().equals(accessoryModel.getFamily()))
                        .noneMatch(playerAccessoryModel -> playerAccessoryModel.getFamilyRank() >= accessoryModel.getFamilyRank());
                })
                .filter(accessoryModel -> {
                    if (Objects.isNull(accessoryModel.getFamily()))
                        return true;

                    return allAccessories.stream()
                        .filter(AccessoryModel::isAttainable)
                        .filter(compareAccessoryModel -> Objects.nonNull(compareAccessoryModel.getFamily()))
                        .filter(compareAccessoryModel -> compareAccessoryModel.getFamily().equals(accessoryModel.getFamily()))
                        .allMatch(compareAccessoryModel -> accessoryModel.getFamilyRank() >= compareAccessoryModel.getFamilyRank());
                })
                .collect(Concurrent.toList());

            // Load Unwanted Accessories
            ConcurrentList<AccessoryModel> unwantedAccessories = playerStats.getAccessories()
                .stream()
                .filter(accessoryData -> !playerStats.getFilteredAccessories().contains(accessoryData))
                .map(AccessoryData::getAccessory)
                .collect(Concurrent.toList());

            // Load Missing Stackable Accessories
            ConcurrentList<AccessoryModel> stackableAccessories = allAccessories.matchAll(
                    SearchFunction.Match.ANY,
                    accessoryModel -> accessoryModel.getFamily().isStatsStackable(),
                    accessoryModel -> accessoryModel.getFamily().isReforgesStackable()
                )
                .stream()
                .filter(accessoryModel -> !playerStats.getFilteredAccessories().contains(accessoryData -> accessoryData.getAccessory().equals(accessoryModel), true))
                .collect(Concurrent.toList())
                .sort(accessoryModel -> accessoryModel.getFamily().getKey(), AccessoryModel::getFamilyRank);

            commandContext.reply(
                Response.builder()
                    .withReference(commandContext)
                    .isInteractable()
                    .withPages(
                        Page.create()
                            .withOption(
                                SelectMenu.Option.builder()
                                    .withValue("missing")
                                    .withLabel("Missing Accessories")
                                    .build()
                            )
                            .withItemsPerPage(10)
                            .withItems(
                                missingAccessories.stream()
                                    .map(accessoryModel -> PageItem.builder()
                                        .withValue(accessoryModel.getItem().getItemId())
                                        .withLabel(accessoryModel.getName())
                                        .build()
                                    )
                                    .collect(Concurrent.toList())
                            )
                            .build(),
                        Page.create()
                            .withOption(
                                SelectMenu.Option.builder()
                                    .withValue("unwanted")
                                    .withLabel("Unwanted Accessories")
                                    .build()
                            )
                            .withItemsPerPage(10)
                            .withItems(
                                unwantedAccessories.stream()
                                    .map(accessoryModel -> PageItem.builder()
                                        .withValue(accessoryModel.getItem().getItemId())
                                        .withLabel(accessoryModel.getName())
                                        .build()
                                    )
                                    .collect(Concurrent.toList())
                            )
                            .build(),
                        Page.create()
                            .withOption(
                                SelectMenu.Option.builder()
                                    .withValue("stackable")
                                    .withLabel("Missing Stackable Accessories")
                                    .build()
                            )
                            .withItemsPerPage(10)
                            .withItems(
                                stackableAccessories.stream()
                                    .map(accessoryModel -> PageItem.builder()
                                        .withValue(accessoryModel.getItem().getItemId())
                                        .withLabel(accessoryModel.getName())
                                        .build()
                                    )
                                    .collect(Concurrent.toList())
                            )
                            .build()
                    )
                    .build()
            );
        });
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            new Parameter(
                "name",
                "Minecraft Username or UUID",
                Parameter.Type.WORD,
                false,
                (argument, commandContext) -> StringUtil.isUUID(argument) || PlayerCommand.MOJANG_NAME.matcher(argument).matches()
            ),
            new Parameter(
                "profile",
                "SkyBlock Profile Name",
                Parameter.Type.WORD,
                false,
                (argument, commandContext) -> SimplifiedApi.getRepositoryOf(ProfileModel.class).findFirst(ProfileModel::getKey, argument.toUpperCase()).isPresent()
            )
        );
    }

}
