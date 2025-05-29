package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.profile_stats.ProfileStats;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.profile_stats.data.AccessoryData;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.data.model.skyblock.accessory_data.accessories.AccessoryModel;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Structure;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.handler.item.ItemHandler;
import dev.sbs.discordapi.response.handler.item.filter.Filter;
import dev.sbs.discordapi.response.handler.item.sorter.Sorter;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.item.field.StringItem;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Structure(
    name = "missing",
    description = "View Missing Accessories"
)
public class MissingCommand extends SkyBlockUserCommand {

    protected MissingCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull SlashCommandContext commandContext, @NotNull SkyBlockUser skyBlockUser) {
        ProfileStats profileStats = skyBlockUser.getSelectedIsland().getProfileStats(skyBlockUser.getMember());
        ConcurrentList<AccessoryModel> allAccessories = SimplifiedApi.getRepositoryOf(AccessoryModel.class)
            .matchAll(accessoryModel -> accessoryModel.getItem().isObtainable())
            .collect(Concurrent.toList());

        return commandContext.reply(
            Response.builder()
                //.isInteractable()
                .withTimeToLive(30)
                .withPages(
                    Page.builder()
                        .withItemHandler(
                            ItemHandler.<AccessoryModel>builder()
                                .withItems(allAccessories)
                                .withFilters(
                                    Filter.<AccessoryModel>builder()
                                        .withTriPredicates((accessoryModel, index, size) -> profileStats.getAccessoryBag()
                                            .getAccessories()
                                            .stream()
                                            .noneMatch(accessoryData -> accessoryData.getAccessory().equals(accessoryModel))
                                        )
                                        .build(),
                                    Filter.<AccessoryModel>builder()
                                        .withTriPredicates((accessoryModel, index, size) -> {
                                            if (Objects.isNull(accessoryModel.getFamily()))
                                                return true;

                                            return profileStats.getAccessoryBag()
                                                .getAccessories()
                                                .stream()
                                                .map(AccessoryData::getAccessory)
                                                .filter(playerAccessoryModel -> Objects.nonNull(playerAccessoryModel.getFamily()))
                                                .filter(playerAccessoryModel -> playerAccessoryModel.getFamily().equals(accessoryModel.getFamily()))
                                                .noneMatch(playerAccessoryModel -> playerAccessoryModel.getFamilyRank() >= accessoryModel.getFamilyRank());
                                        })
                                        .build(),
                                    Filter.<AccessoryModel>builder()
                                        .withTriPredicates((accessoryModel, index, size) -> {
                                            if (Objects.isNull(accessoryModel.getFamily()))
                                                return true;

                                            return allAccessories.stream()
                                                .filter(compareAccessoryModel -> compareAccessoryModel.getItem().isObtainable())
                                                .filter(compareAccessoryModel -> Objects.nonNull(compareAccessoryModel.getFamily()))
                                                .filter(compareAccessoryModel -> compareAccessoryModel.getFamily().equals(accessoryModel.getFamily()))
                                                .allMatch(compareAccessoryModel -> accessoryModel.getFamilyRank() >= compareAccessoryModel.getFamilyRank());
                                        })
                                        .build()
                                )
                                .withTransformer((accessoryModel, index, size) -> StringItem.builder()
                                    .withEmoji(
                                        skyBlockUser.getSkyBlockEmojis()
                                            .getEmoji(accessoryModel.getItem().getItemId())
                                            .map(Emoji::of)
                                    )
                                    .withValue(accessoryModel.getItem().getItemId())
                                    .withLabel(accessoryModel.getName())
                                    .build()
                                )
                                .withSorters(
                                    Sorter.<AccessoryModel>builder()
                                        .withFunctions(AccessoryModel::getName)
                                        .build()
                                )
                                .withFieldStyle(ItemHandler.FieldStyle.LIST)
                                .withAmountPerPage(10)
                                .build()
                        )
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("missing")
                                .withLabel("Missing Accessories")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "missing_accessories")
                                .withDescription("You are missing ${FILTERED_SIZE} accessories.")
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItemHandler(
                            ItemHandler.<AccessoryModel>builder()
                                .withItems(allAccessories)
                                .withFilters(
                                    Filter.<AccessoryModel>builder()
                                        .withLabel("Stackable Stats")
                                        .withPredicates(accessoryModel -> accessoryModel.getFamily().isStatsStackable() ||
                                            accessoryModel.getFamily().isReforgesStackable()
                                        )
                                        .build(),
                                    Filter.<AccessoryModel>builder()
                                        .withLabel("I don't even know")
                                        .withPredicates(accessoryModel -> accessoryModel.getFamily().isStatsStackable() ||
                                            accessoryModel.getFamily().isReforgesStackable()
                                        )
                                        .withPredicates(accessoryModel -> !profileStats.getAccessoryBag()
                                            .getFilteredAccessories()
                                            .contains(accessoryData -> accessoryData.getAccessory().equals(accessoryModel), true)
                                        )
                                        .build()
                                )
                                .withTransformer((accessoryModel, index, size) -> StringItem.builder()
                                    .withEmoji(
                                        skyBlockUser.getSkyBlockEmojis()
                                            .getEmoji(accessoryModel.getItem().getItemId())
                                            .map(Emoji::of)
                                    )
                                    .withValue(accessoryModel.getItem().getItemId())
                                    .withLabel(accessoryModel.getName())
                                    .build()
                                )
                                .withSorters(
                                    Sorter.<AccessoryModel>builder()
                                        .withFunctions(accessoryModel -> accessoryModel.getFamily().getKey())
                                        .withFunctions(AccessoryModel::getFamilyRank)
                                        .build()
                                )
                                .withFieldStyle(ItemHandler.FieldStyle.LIST)
                                .withAmountPerPage(10)
                                .build()
                        )
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("stackable")
                                .withLabel("Missing Stackable Accessories")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "stackable_accessories")
                                .withDescription("You are missing ${FILTERED_SIZE} stackable accessories.")
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItemHandler(
                            ItemHandler.<AccessoryData>builder()
                                .withItems(profileStats.getAccessoryBag().getAccessories())
                                .withFilters(
                                    Filter.<AccessoryData>builder()
                                        .withLabel("I Forget")
                                        .withPredicates(accessoryData -> !profileStats.getAccessoryBag()
                                            .getFilteredAccessories()
                                            .contains(accessoryData)
                                        )
                                        .build()
                                )
                                .withTransformer((accessoryData, index, size) -> StringItem.builder()
                                    .withEmoji(
                                        skyBlockUser.getSkyBlockEmojis()
                                            .getEmoji(accessoryData.getAccessory().getItem().getItemId())
                                            .map(Emoji::of)
                                    )
                                    .withValue(accessoryData.getAccessory().getItem().getItemId())
                                    .withLabel(accessoryData.getAccessory().getName())
                                    .build()
                                )
                                .withFieldStyle(ItemHandler.FieldStyle.LIST)
                                .withAmountPerPage(10)
                                .build()
                        )
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("unwanted")
                                .withLabel("Unwanted Accessories")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "unwanted_accessories")
                                .withDescription("You have ${FILTERED_SIZE} unwanted accessories.")
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItemHandler(
                            ItemHandler.<AccessoryData>builder()
                                .withItems(profileStats.getAccessoryBag().getFilteredAccessories())
                                .withFilters(
                                    Filter.<AccessoryData>builder()
                                        .withLabel("Not Recombobulated")
                                        .withPredicates(AccessoryData::notRecombobulated)
                                        .build()
                                )
                                .withTransformer((accessoryData, index, size) -> StringItem.builder()
                                    .withEmoji(
                                        skyBlockUser.getSkyBlockEmojis()
                                            .getEmoji(accessoryData.getAccessory().getItem().getItemId())
                                            .map(Emoji::of)
                                    )
                                    .withValue(accessoryData.getAccessory().getItem().getItemId())
                                    .withLabel(accessoryData.getAccessory().getName())
                                    .build()
                                )
                                .withFieldStyle(ItemHandler.FieldStyle.LIST)
                                .withAmountPerPage(10)
                                .build()
                        )
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("recombobulators")
                                .withLabel("Missing Recombobulators")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "missing_recombobulators")
                                .withDescription("You are missing recombobulators on ${FILTERED_SIZE} accessories.")
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItemHandler(
                            ItemHandler.<AccessoryData>builder()
                                .withItems(profileStats.getAccessoryBag().getFilteredAccessories())
                                .withFilters(
                                    Filter.<AccessoryData>builder()
                                        .withLabel("Missing Enrichment")
                                        .withPredicates(AccessoryData::isMissingEnrichment)
                                        .build()
                                )
                                .withTransformer((accessoryData, index, size) -> StringItem.builder()
                                    .withEmoji(
                                        skyBlockUser.getSkyBlockEmojis()
                                            .getEmoji(accessoryData.getAccessory().getItem().getItemId())
                                            .map(Emoji::of)
                                    )
                                    .withValue(accessoryData.getAccessory().getItem().getItemId())
                                    .withLabel(accessoryData.getAccessory().getName())
                                    .build()
                                )
                                .withFieldStyle(ItemHandler.FieldStyle.LIST)
                                .withAmountPerPage(10)
                                .build()
                        )
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("enrichments")
                                .withLabel("Missing Enrichments")
                                .build()
                        )
                        .withEmbeds(
                            getEmbedBuilder(skyBlockUser.getMojangProfile(), skyBlockUser.getSelectedIsland(), "missing_enrichments")
                                .withDescription("You are missing enrichments on ${FILTERED_SIZE} accessories.")
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

}
