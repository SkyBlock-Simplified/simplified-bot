package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.profile_stats.ProfileStats;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.profile_stats.data.AccessoryData;
import dev.sbs.api.data.model.skyblock.accessory_data.accessories.AccessoryModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.handler.cache.ItemHandler;
import dev.sbs.discordapi.response.page.handler.sorter.Sorter;
import dev.sbs.discordapi.response.page.item.Item;
import dev.sbs.discordapi.response.page.item.field.StringItem;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Objects;

@CommandId("b0e6bdee-971c-4774-9373-a8ef3ccd4e5b")
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
                .isInteractable()
                .withTimeToLive(30)
                .withPages(
                    Page.builder()
                        .withItemHandler(
                            ItemHandler.builder(AccessoryModel.class)
                                .withItems(allAccessories)
                                .withFilters(
                                    (accessoryModel, index, size) -> profileStats.getAccessoryBag()
                                        .getAccessories()
                                        .stream()
                                        .noneMatch(accessoryData -> accessoryData.getAccessory().equals(accessoryModel)),
                                    (accessoryModel, index, size) -> {
                                        if (Objects.isNull(accessoryModel.getFamily()))
                                            return true;

                                        return profileStats.getAccessoryBag()
                                            .getAccessories()
                                            .stream()
                                            .map(AccessoryData::getAccessory)
                                            .filter(playerAccessoryModel -> Objects.nonNull(playerAccessoryModel.getFamily()))
                                            .filter(playerAccessoryModel -> playerAccessoryModel.getFamily().equals(accessoryModel.getFamily()))
                                            .noneMatch(playerAccessoryModel -> playerAccessoryModel.getFamilyRank() >= accessoryModel.getFamilyRank());
                                    },
                                    (accessoryModel, index, size) -> {
                                        if (Objects.isNull(accessoryModel.getFamily()))
                                            return true;

                                        return allAccessories.stream()
                                            .filter(compareAccessoryModel -> compareAccessoryModel.getItem().isObtainable())
                                            .filter(compareAccessoryModel -> Objects.nonNull(compareAccessoryModel.getFamily()))
                                            .filter(compareAccessoryModel -> compareAccessoryModel.getFamily().equals(accessoryModel.getFamily()))
                                            .allMatch(compareAccessoryModel -> accessoryModel.getFamilyRank() >= compareAccessoryModel.getFamilyRank());
                                    })
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
                            ItemHandler.builder(AccessoryModel.class)
                                .withItems(allAccessories)
                                .withFilters((accessoryModel, index, size) -> accessoryModel.getFamily().isStatsStackable() ||
                                    accessoryModel.getFamily().isReforgesStackable()
                                )
                                .withFilters((accessoryModel, index, size) -> !profileStats.getAccessoryBag()
                                    .getFilteredAccessories()
                                    .contains(accessoryData -> accessoryData.getAccessory().equals(accessoryModel), true)
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
                                .withStyle(Item.Style.LIST)
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
                            ItemHandler.builder(AccessoryData.class)
                                .withItems(profileStats.getAccessoryBag().getAccessories())
                                .withFilters((accessoryData, aLong, aLong2) -> !profileStats.getAccessoryBag().getFilteredAccessories().contains(accessoryData))
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
                                .withStyle(Item.Style.LIST_SINGLE)
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
                            ItemHandler.builder(AccessoryData.class)
                                .withItems(profileStats.getAccessoryBag().getFilteredAccessories())
                                .withFilters((accessoryData, index, size) -> accessoryData.notRecombobulated())
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
                                .withStyle(Item.Style.LIST)
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
                            ItemHandler.builder(AccessoryData.class)
                                .withItems(profileStats.getAccessoryBag().getFilteredAccessories())
                                .withFilters((accessoryData, index, size) -> accessoryData.isMissingEnrichment())
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
                                .withStyle(Item.Style.LIST)
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
