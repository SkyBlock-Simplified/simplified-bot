package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.AccessoryData;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.ObjectData;
import dev.sbs.api.data.model.skyblock.accessory_data.accessories.AccessoryModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.search.function.SearchFunction;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.item.FieldItem;
import dev.sbs.discordapi.response.page.item.PageItem;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Objects;

@CommandInfo(
    id = "b0e6bdee-971c-4774-9373-a8ef3ccd4e5b",
    name = "missing"
)
public class MissingCommand extends SkyBlockUserCommand {

    protected MissingCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser) {
        PlayerStats playerStats = skyBlockUser.getSelectedIsland().getPlayerStats(skyBlockUser.getMember());
        ConcurrentList<AccessoryModel> allAccessories = SimplifiedApi.getRepositoryOf(AccessoryModel.class).findAll();

        ConcurrentList<FieldItem> missingAccessories = allAccessories.stream()
            .filter(accessoryModel -> accessoryModel.getItem().isObtainable())
            .filter(accessoryModel -> playerStats.getAccessoryBag()
                .getAccessories()
                .stream()
                .noneMatch(accessoryData -> accessoryData.getAccessory().equals(accessoryModel))
            )
            .filter(accessoryModel -> {
                if (Objects.isNull(accessoryModel.getFamily()))
                    return true;

                return playerStats.getAccessoryBag()
                    .getAccessories()
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
                    .filter(compareAccessoryModel -> compareAccessoryModel.getItem().isObtainable())
                    .filter(compareAccessoryModel -> Objects.nonNull(compareAccessoryModel.getFamily()))
                    .filter(compareAccessoryModel -> compareAccessoryModel.getFamily().equals(accessoryModel.getFamily()))
                    .allMatch(compareAccessoryModel -> accessoryModel.getFamilyRank() >= compareAccessoryModel.getFamilyRank());
            })
            .map(accessoryModel -> FieldItem.builder()
                .withEmoji(
                    skyBlockUser.getSkyBlockEmojis()
                        .getEmoji(accessoryModel.getItem().getItemId())
                        .map(Emoji::of)
                )
                .withOptionValue(accessoryModel.getItem().getItemId())
                .withLabel(accessoryModel.getName())
                .build()
            )
            .collect(Concurrent.toList())
            .sorted(pageItem -> pageItem.getOption().orElseThrow().getLabel());

        ConcurrentList<FieldItem> unwantedAccessories = playerStats.getAccessoryBag()
            .getAccessories()
            .stream()
            .filter(accessoryData -> !playerStats.getAccessoryBag().getFilteredAccessories().contains(accessoryData))
            .map(AccessoryData::getAccessory)
            .map(accessoryModel -> FieldItem.builder()
                .withEmoji(
                    skyBlockUser.getSkyBlockEmojis()
                        .getEmoji(accessoryModel.getItem().getItemId())
                        .map(Emoji::of)
                )
                .withOptionValue(accessoryModel.getItem().getItemId())
                .withLabel(accessoryModel.getName())
                .build()
            )
            .collect(Concurrent.toList());

        ConcurrentList<FieldItem> stackableAccessories = allAccessories.matchAll(
                SearchFunction.Match.ANY,
                accessoryModel -> accessoryModel.getFamily().isStatsStackable(),
                accessoryModel -> accessoryModel.getFamily().isReforgesStackable()
            )
            .stream()
            .filter(accessoryModel -> !playerStats.getAccessoryBag().getFilteredAccessories().contains(accessoryData -> accessoryData.getAccessory().equals(accessoryModel), true))
            .collect(Concurrent.toList())
            .sorted(accessoryModel -> accessoryModel.getFamily().getKey(), AccessoryModel::getFamilyRank)
            .stream()
            .map(accessoryModel -> FieldItem.builder()
                .withEmoji(
                    skyBlockUser.getSkyBlockEmojis()
                        .getEmoji(accessoryModel.getItem().getItemId())
                        .map(Emoji::of)
                )
                .withOptionValue(accessoryModel.getItem().getItemId())
                .withLabel(accessoryModel.getName())
                .build()
            )
            .collect(Concurrent.toList());

        ConcurrentList<FieldItem> missingRecombobulators = playerStats.getAccessoryBag()
            .getFilteredAccessories()
            .stream()
            .filter(ObjectData::notRecombobulated)
            .map(AccessoryData::getAccessory)
            .map(accessoryModel -> FieldItem.builder()
                .withEmoji(
                    skyBlockUser.getSkyBlockEmojis()
                        .getEmoji(accessoryModel.getItem().getItemId())
                        .map(Emoji::of)
                )
                .withOptionValue(accessoryModel.getItem().getItemId())
                .withLabel(accessoryModel.getName())
                .build()
            )
            .collect(Concurrent.toList());

        ConcurrentList<FieldItem> missingEnrichments = playerStats.getAccessoryBag()
            .getFilteredAccessories()
            .stream()
            .filter(AccessoryData::isMissingEnrichment)
            .map(AccessoryData::getAccessory)
            .map(accessoryModel -> FieldItem.builder()
                .withEmoji(
                    skyBlockUser.getSkyBlockEmojis()
                        .getEmoji(accessoryModel.getItem().getItemId())
                        .map(Emoji::of)
                )
                .withOptionValue(accessoryModel.getItem().getItemId())
                .withLabel(accessoryModel.getName())
                .build()
            )
            .collect(Concurrent.toList());

        return commandContext.reply(
            Response.builder()
                .withReference(commandContext)
                .isInteractable()
                .withTimeToLive(30)
                .withPages(
                    Page.builder()
                        .withItems(missingAccessories)
                        .withItemsPerPage(10)
                        .withItemStyle(PageItem.Style.LIST_SINGLE)
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("missing")
                                .withLabel("Missing Accessories")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "missing", "Accessory Information")
                                .withDescription(FormatUtil.format("You are missing {0} accessories.", missingAccessories.size()))
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItems(unwantedAccessories)
                        .withItemsPerPage(10)
                        .withItemStyle(PageItem.Style.LIST_SINGLE)
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("unwanted")
                                .withLabel("Unwanted Accessories")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "unwanted", "Accessory Information")
                                .withDescription(FormatUtil.format("You have {0} unwanted accessories.", unwantedAccessories.size()))
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItems(stackableAccessories)
                        .withItemsPerPage(10)
                        .withItemStyle(PageItem.Style.LIST)
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("stackable")
                                .withLabel("Missing Stackable Accessories")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "stackable", "Accessory Information")
                                .withDescription(FormatUtil.format("You are missing {0} stackable accessories.", stackableAccessories.size()))
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItems(missingRecombobulators)
                        .withItemsPerPage(10)
                        .withItemStyle(PageItem.Style.LIST)
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("recombobulators")
                                .withLabel("Missing Recombobulators")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "recombobulators", "Accessory Information")
                                .withDescription(FormatUtil.format("You are missing recombobulators on {0} accessories.", missingRecombobulators.size()))
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItems(missingEnrichments)
                        .withItemsPerPage(10)
                        .withItemStyle(PageItem.Style.LIST)
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("enrichments")
                                .withLabel("Missing Enrichments")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "enrichments", "Accessory Information")
                                .withDescription(FormatUtil.format("You are missing enrichments on {0} accessories.", missingEnrichments.size()))
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

}
