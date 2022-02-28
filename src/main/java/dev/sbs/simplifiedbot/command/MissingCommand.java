package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.AccessoryData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ObjectData;
import dev.sbs.api.data.model.skyblock.accessories.AccessoryModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.collection.search.function.SearchFunction;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.action.SelectMenu;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.PageItem;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Objects;

@CommandInfo(
    id = "b0e6bdee-971c-4774-9373-a8ef3ccd4e5b",
    name = "missing"
)
public class MissingCommand extends Command {

    protected MissingCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) {
        SkyBlockUser skyBlockUser = new SkyBlockUser(commandContext);
        PlayerStats playerStats = skyBlockUser.getSelectedIsland().getPlayerStats(skyBlockUser.getMember());
        ConcurrentList<AccessoryModel> allAccessories = SimplifiedApi.getRepositoryOf(AccessoryModel.class).findAll();

        return commandContext.reply(
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
                            allAccessories.stream()
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
                            playerStats.getAccessories()
                                .stream()
                                .filter(accessoryData -> !playerStats.getFilteredAccessories().contains(accessoryData))
                                .map(AccessoryData::getAccessory)
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
                            allAccessories.matchAll(
                                    SearchFunction.Match.ANY,
                                    accessoryModel -> accessoryModel.getFamily().isStatsStackable(),
                                    accessoryModel -> accessoryModel.getFamily().isReforgesStackable()
                                )
                                .stream()
                                .filter(accessoryModel -> !playerStats.getFilteredAccessories().contains(accessoryData -> accessoryData.getAccessory().equals(accessoryModel), true))
                                .collect(Concurrent.toList())
                                .sort(accessoryModel -> accessoryModel.getFamily().getKey(), AccessoryModel::getFamilyRank)
                                .stream()
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
                                .withValue("reforges")
                                .withLabel("Missing Reforges")
                                .build()
                        )
                        .withItemsPerPage(10)
                        .withItems(
                            playerStats.getFilteredAccessories()
                                .stream()
                                .filter(accessoryData -> accessoryData.getReforge().isEmpty())
                                .map(AccessoryData::getAccessory)
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
                                .withValue("recombobulators")
                                .withLabel("Missing Recombobulators")
                                .build()
                        )
                        .withItemsPerPage(10)
                        .withItems(
                            playerStats.getFilteredAccessories()
                                .stream()
                                .filter(ObjectData::notRecombobulated)
                                .map(AccessoryData::getAccessory)
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
                                .withValue("enrichments")
                                .withLabel("Missing Enrichments")
                                .build()
                        )
                        .withItemsPerPage(10)
                        .withItems(
                            playerStats.getFilteredAccessories()
                                .stream()
                                .filter(accessoryData -> accessoryData.getEnrichment().isEmpty())
                                .map(AccessoryData::getAccessory)
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
