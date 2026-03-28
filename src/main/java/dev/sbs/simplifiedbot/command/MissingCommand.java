package dev.sbs.simplifiedbot.command;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Structure;
import dev.sbs.discordapi.component.interaction.SelectMenu;
import dev.sbs.discordapi.context.command.SlashCommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.handler.Filter;
import dev.sbs.discordapi.response.handler.Sorter;
import dev.sbs.discordapi.response.handler.item.ItemHandler;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.item.field.StringItem;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.model.Accessory;
import dev.sbs.simplifiedbot.profile_stats.ProfileStats;
import dev.sbs.simplifiedbot.profile_stats.data.AccessoryData;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

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
        ConcurrentList<Accessory> allAccessories = MinecraftApi.getRepository(Accessory.class)
            .matchAll(accessoryModel -> accessoryModel.getItem().getAttributes().isObtainable())
            .collect(Concurrent.toList());

        return commandContext.reply(
            Response.builder()
                //.isInteractable()
                .withTimeToLive(30)
                .withPages(
                    Page.builder()
                        .withItemHandler(
                            ItemHandler.<Accessory>embed()
                                .withItems(allAccessories)
                                .withFilters(
                                    Filter.<Accessory>builder()
                                        .withTriPredicates((accessoryModel, index, size) -> profileStats.getAccessoryBag()
                                            .getAccessories()
                                            .stream()
                                            .noneMatch(accessoryData -> accessoryData.getAccessory().equals(accessoryModel))
                                        )
                                        .build(),
                                    Filter.<Accessory>builder()
                                        .withTriPredicates((accessoryModel, index, size) -> {
                                            if (accessoryModel.getFamily().isEmpty())
                                                return true;

                                            return profileStats.getAccessoryBag()
                                                .getAccessories()
                                                .stream()
                                                .map(AccessoryData::getAccessory)
                                                .filter(playerAccessory -> playerAccessory.getFamily().isPresent())
                                                .filter(playerAccessory -> playerAccessory.getFamily().equals(accessoryModel.getFamily()))
                                                .noneMatch(playerAccessory -> playerAccessory.getFamily().get().getRank() >= accessoryModel.getFamily().get().getRank());
                                        })
                                        .build(),
                                    Filter.<Accessory>builder()
                                        .withTriPredicates((accessoryModel, index, size) -> {
                                            if (accessoryModel.getFamily().isEmpty())
                                                return true;

                                            return allAccessories.stream()
                                                .filter(compareAccessory -> compareAccessory.getItem().getAttributes().isObtainable())
                                                .filter(compareAccessory -> compareAccessory.getFamily().isPresent())
                                                .filter(compareAccessory -> compareAccessory.getFamily().equals(accessoryModel.getFamily()))
                                                .allMatch(compareAccessory -> accessoryModel.getFamily().get().getRank() >= compareAccessory.getFamily().get().getRank());
                                        })
                                        .build()
                                )
                                .withTransformer((accessoryModel, index, size) -> StringItem.builder()
                                    .withEmoji(
                                        skyBlockUser.getSkyBlockEmojis()
                                            .getEmoji(accessoryModel.getItem().getId())
                                            .map(SkyBlockUserCommand::getEmoji)
                                    )
                                    .withValue(accessoryModel.getItem().getId())
                                    .withLabel(accessoryModel.getItem().getDisplayName())
                                    .build()
                                )
                                .withSorters(
                                    Sorter.<Accessory>builder()
                                        .withFunctions(accessory -> accessory.getItem().getDisplayName())
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
                            ItemHandler.<Accessory>embed()
                                .withItems(allAccessories)
                                .withFilters(
                                    Filter.<Accessory>builder()
                                        .withLabel("Stackable Stats")
                                        .withPredicates(accessoryModel -> accessoryModel.getFamily().isPresent()
                                        )
                                        .build(),
                                    Filter.<Accessory>builder()
                                        .withLabel("Stackable Reforges")
                                        .withPredicates(accessoryModel -> accessoryModel.getFamily().isPresent()
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
                                            .getEmoji(accessoryModel.getItem().getId())
                                            .map(SkyBlockUserCommand::getEmoji)
                                    )
                                    .withValue(accessoryModel.getItem().getId())
                                    .withLabel(accessoryModel.getItem().getDisplayName())
                                    .build()
                                )
                                .withSorters(
                                    Sorter.<Accessory>builder()
                                        .withFunctions(accessoryModel -> accessoryModel.getFamily().map(Accessory.Family::getId).orElse(""))
                                        .withFunctions(accessory -> accessory.getFamily().map(Accessory.Family::getRank).orElse(0))
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
                            ItemHandler.<AccessoryData>embed()
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
                                            .getEmoji(accessoryData.getAccessory().getItem().getId())
                                            .map(SkyBlockUserCommand::getEmoji)
                                    )
                                    .withValue(accessoryData.getAccessory().getItem().getId())
                                    .withLabel(accessoryData.getAccessory().getItem().getDisplayName())
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
                            ItemHandler.<AccessoryData>embed()
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
                                            .getEmoji(accessoryData.getAccessory().getItem().getId())
                                            .map(SkyBlockUserCommand::getEmoji)
                                    )
                                    .withValue(accessoryData.getAccessory().getItem().getId())
                                    .withLabel(accessoryData.getAccessory().getItem().getDisplayName())
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
                            ItemHandler.<AccessoryData>embed()
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
                                            .getEmoji(accessoryData.getAccessory().getItem().getId())
                                            .map(SkyBlockUserCommand::getEmoji)
                                    )
                                    .withValue(accessoryData.getAccessory().getItem().getId())
                                    .withLabel(accessoryData.getAccessory().getItem().getDisplayName())
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
