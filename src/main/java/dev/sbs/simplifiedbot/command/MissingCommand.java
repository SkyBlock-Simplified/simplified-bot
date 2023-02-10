package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.playerstats.PlayerStats;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.playerstats.data.AccessoryData;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.playerstats.data.ObjectData;
import dev.sbs.api.data.model.skyblock.accessory_data.accessories.AccessoryModel;
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
        ConcurrentList<AccessoryModel> allAccessories = SimplifiedApi.getRepositoryOf(AccessoryModel.class).matchAll(accessoryModel -> accessoryModel.getItem().isObtainable());

        return commandContext.reply(
            Response.builder()
                .withReference(commandContext)
                .isInteractable()
                .withTimeToLive(30)
                .withPages(
                    Page.builder()
                        .withItemData(
                            Page.ItemData.builder(AccessoryModel.class)
                                .withFieldItems(allAccessories)
                                .withTransformer(stream -> stream
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
                                )
                                .withSorters(
                                    Page.ItemData.Sorter.<AccessoryModel>builder()
                                        .withFunctions(AccessoryModel::getName)
                                        .build()
                                )
                                .withStyle(PageItem.Style.LIST_SINGLE)
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
                                .withDescription(FormatUtil.format("You are missing {0} accessories.", -1))
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItemData(
                            Page.ItemData.builder(AccessoryModel.class)
                                .withFieldItems(allAccessories.matchAll(
                                    SearchFunction.Match.ANY,
                                    accessoryModel -> accessoryModel.getFamily().isStatsStackable(),
                                    accessoryModel -> accessoryModel.getFamily().isReforgesStackable()
                                ))
                                .withTransformer(stream -> stream
                                    .filter(accessoryModel -> !playerStats.getAccessoryBag()
                                        .getFilteredAccessories()
                                        .contains(accessoryData -> accessoryData.getAccessory().equals(accessoryModel), true)
                                    )
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
                                )
                                .withSorters(
                                    Page.ItemData.Sorter.<AccessoryModel>builder()
                                        .withFunctions(accessoryModel -> accessoryModel.getFamily().getKey())
                                        .withFunctions(AccessoryModel::getFamilyRank)
                                        .build()
                                )
                                .withStyle(PageItem.Style.LIST)
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
                                .withDescription(FormatUtil.format("You are missing {0} stackable accessories.", -1))
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItemData(
                            Page.ItemData.builder(AccessoryData.class)
                                .withFieldItems(playerStats.getAccessoryBag().getAccessories())
                                .withTransformer(stream -> stream
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
                                )
                                .withStyle(PageItem.Style.LIST_SINGLE)
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
                                .withDescription(FormatUtil.format("You have {0} unwanted accessories.", -1))
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItemData(
                            Page.ItemData.builder(AccessoryData.class)
                                .withFieldItems(playerStats.getAccessoryBag().getFilteredAccessories())
                                .withTransformer(stream -> stream
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
                                )
                                .withStyle(PageItem.Style.LIST)
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
                                .withDescription(FormatUtil.format("You are missing recombobulators on {0} accessories.", -1))
                                .build()
                        )
                        .build(),
                    Page.builder()
                        .withItemData(
                            Page.ItemData.builder(AccessoryData.class)
                                .withFieldItems(playerStats.getAccessoryBag().getFilteredAccessories())
                                .withTransformer(stream -> stream
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
                                )
                                .withStyle(PageItem.Style.LIST)
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
                                .withDescription(FormatUtil.format("You are missing enrichments on {0} accessories.", -1))
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

}
