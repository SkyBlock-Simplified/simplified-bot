package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockAuction;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.data.AccessoryData;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.skyblock.accessory_data.accessory_enrichments.AccessoryEnrichmentModel;
import dev.sbs.api.data.model.skyblock.collection_data.collection_items.CollectionItemModel;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeon_classes.DungeonClassModel;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeons.DungeonModel;
import dev.sbs.api.data.model.skyblock.items.ItemModel;
import dev.sbs.api.data.model.skyblock.minion_data.minion_uniques.MinionUniqueModel;
import dev.sbs.api.data.model.skyblock.pet_data.pets.PetModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.data.model.skyblock.shop_data.shop_profile_upgrades.ShopProfileUpgradeModel;
import dev.sbs.api.data.model.skyblock.skills.SkillModel;
import dev.sbs.api.data.model.skyblock.slayers.SlayerModel;
import dev.sbs.api.data.model.skyblock.stats.StatModel;
import dev.sbs.api.minecraft.nbt.tags.collection.CompoundTag;
import dev.sbs.api.minecraft.nbt.tags.primitive.StringTag;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.collection.search.function.SearchFunction;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.StreamUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.item.FieldItem;
import dev.sbs.discordapi.response.page.item.PageItem;
import dev.sbs.discordapi.util.DiscordDate;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

@CommandInfo(
    id = "733e6780-84cd-45ed-921a-9b1ca9b02ed6",
    name = "player"
)
public class PlayerCommand extends SkyBlockUserCommand {

    protected PlayerCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return commandContext.reply(
            Response.builder()
                .isInteractable()
                .replyMention()
                .withReference(commandContext)
                .withTimeToLive(30)
                .withPages(buildPages(skyBlockUser))
                .build()
        );
    }

    public static @NotNull ConcurrentList<Page> buildPages(@NotNull SkyBlockUser skyBlockUser) {
        String emojiReplyStem = getEmoji("REPLY_STEM").map(Emoji::asPreSpacedFormat).orElse("");
        String emojiReplyLine = getEmoji("REPLY_LINE").map(Emoji::asPreSpacedFormat).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(Emoji::asPreSpacedFormat).orElse("");
        MojangProfileResponse mojangProfile = skyBlockUser.getMojangProfile();
        SkyBlockIsland skyBlockIsland = skyBlockUser.getSelectedIsland();
        SkyBlockIsland.Member member = skyBlockUser.getMember();
        int uniqueMinions = skyBlockIsland.getMinions()
            .stream()
            .mapToInt(minion -> minion.getUnlocked().size())
            .sum();

        // Weights
        SkyBlockIsland.Experience.Weight totalWeight = member.getTotalWeight();
        ConcurrentMap<SkyBlockIsland.Skill, SkyBlockIsland.Experience.Weight> skillWeight = member.getSkillWeight();
        ConcurrentMap<SkyBlockIsland.Slayer, SkyBlockIsland.Experience.Weight> slayerWeight = member.getSlayerWeight();
        ConcurrentMap<SkyBlockIsland.Dungeon, SkyBlockIsland.Experience.Weight> dungeonWeight = member.getDungeonWeight();
        ConcurrentMap<SkyBlockIsland.Dungeon.Class, SkyBlockIsland.Experience.Weight> dungeonClassWeight = member.getDungeonClassWeight();

        return Concurrent.newList(
            Page.builder()
                .withOption(getOptionBuilder("stats").withEmoji(Emoji.of(skyBlockIsland.getProfileName().map(ProfileModel::getEmoji).get())).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "stats", "Player Information")
                        .withFields(
                            Field.builder()
                                .withEmoji(getEmoji("STATUS_INFO"))
                                .withName("Details")
                                .withValue(
                                    """
                                        {0}Status: {2}
                                        {0}Deaths: {3}
                                        {1}Guild: {4}
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    skyBlockUser.getSession().isOnline() ? "Online" : "Offline",
                                    member.getDeathCount(),
                                    skyBlockUser.getGuild().map(HypixelGuildResponse.Guild::getName).orElse("None")
                                )
                                .isInline()
                                .build(),
                            Field.builder()
                                .withEmoji(getEmoji("TRADING_COIN_PIGGY"))
                                .withName("Coins")
                                .withValue(
                                    """
                                        {0}Bank: {2,number,#,###}
                                        {1}Purse: {3,number,#,###}
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    skyBlockIsland.getBanking().map(SkyBlockIsland.Banking::getBalance).orElse(0.0),
                                    member.getPurse()
                                )
                                .isInline()
                                .build(),
                            Field.empty(true)
                        )
                        .withFields(
                            Field.builder()
                                .withEmoji(getEmoji("GEM_EMERALD"))
                                .withName("Community Upgrades")
                                .withValue(
                                    StringUtil.join(
                                        StreamUtil.prependEach(
                                                SimplifiedApi.getRepositoryOf(ShopProfileUpgradeModel.class)
                                                    .stream()
                                                    .map(shopProfileUpgradeModel -> FormatUtil.format(
                                                        "{0}: {1} / {2}",
                                                        shopProfileUpgradeModel.getName(),
                                                        skyBlockIsland.getCommunityUpgrades()
                                                            .map(communityUpgrades -> communityUpgrades.getHighestTier(shopProfileUpgradeModel))
                                                            .orElse(0),
                                                        shopProfileUpgradeModel.getMaxLevel()
                                                    )),
                                                emojiReplyStem,
                                                emojiReplyEnd
                                            )
                                            .collect(Concurrent.toList()),
                                        "\n"
                                    )
                                )
                                .isInline()
                                .build(),
                            Field.builder()
                                .withEmoji(getEmoji("SKYBLOCK_LAPIS_MINION"))
                                .withName("Minions")
                                .withValue(
                                    """
                                        {0}Slots: {2}
                                        {1}Uniques: {3}
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    SimplifiedApi.getRepositoryOf(MinionUniqueModel.class)
                                        .matchLast(minionUniqueModel -> uniqueMinions >= minionUniqueModel.getUniqueCrafts())
                                        .map(MinionUniqueModel::getPlaceable)
                                        .orElse(0) +
                                        skyBlockIsland.getCommunityUpgrades()
                                            .stream()
                                            .mapToInt(communityUpgrades -> SimplifiedApi.getRepositoryOf(ShopProfileUpgradeModel.class)
                                                .findFirst(ShopProfileUpgradeModel::getKey, "MINION_SLOTS")
                                                .map(communityUpgrades::getHighestTier)
                                                .orElse(0)
                                            )
                                            .sum(),
                                    uniqueMinions
                                )
                                .isInline()
                                .build(),
                            Field.empty(true)
                        )
                        .build()
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("skills").withEmoji(getEmoji("SKILLS")).build())
                .withEmbeds(
                    getSkillEmbed(
                        mojangProfile,
                        skyBlockIsland,
                        "skills",
                        "Skill Information",
                        member.getSkills(),
                        member.getSkillAverage(),
                        member.getSkillExperience(),
                        member.getSkillProgressPercentage(),
                        skill -> skill.getType().getName(),
                        skill -> Emoji.of(skill.getType().getEmoji()),
                        true
                    )
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("slayers").withEmoji(getEmoji("SLAYER")).build())
                .withEmbeds(
                    getSkillEmbed(
                        mojangProfile,
                        skyBlockIsland,
                        "slayers",
                        "Slayer Information",
                        member.getSlayers().inverse(),
                        member.getSlayerAverage(),
                        member.getSlayerExperience(),
                        member.getSlayerProgressPercentage(),
                        slayer -> slayer.getType().getName(),
                        slayer -> Emoji.of(slayer.getType().getEmoji()),
                        true
                    )
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("weight").withEmoji(getEmoji("WEIGHT")).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "weight", "Player Information")
                        .withDescription(
                            """
                                {0}Total Weight: **{2}** (**{3}** + **{4}**)
                                {0}Total Skill Weight: **{5}** (**{6}** + **{7}**)
                                {0}Total Slayer Weight: **{8}** (**{9}** + **{10}**)
                                {0}Total Dungeon Weight: **{11}** (**{12}** + **{13}**)
                                {1}Total Dungeon Class Weight: **{14}** (**{15}** + **{16}**)""",
                            emojiReplyStem,
                            emojiReplyEnd,
                            totalWeight.getTotal(),
                            totalWeight.getValue(),
                            totalWeight.getOverflow(),
                            skillWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getTotal)
                                .sum(),
                            skillWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getValue)
                                .sum(),
                            skillWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getOverflow)
                                .sum(),
                            slayerWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getTotal)
                                .sum(),
                            slayerWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getValue)
                                .sum(),
                            slayerWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getOverflow)
                                .sum(),
                            dungeonWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getTotal)
                                .sum(),
                            dungeonWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getValue)
                                .sum(),
                            dungeonWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getValue)
                                .sum(),
                            dungeonClassWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getTotal)
                                .sum(),
                            dungeonClassWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getValue)
                                .sum(),
                            dungeonClassWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getOverflow)
                                .sum()
                        )
                        .withFields(
                            getWeightFields(
                                "Skills",
                                skillWeight,
                                SearchFunction.combine(
                                    SkyBlockIsland.Skill::getType,
                                    SkillModel::getName
                                )
                            )
                        )
                        .withFields(
                            getWeightFields(
                                "Slayers",
                                slayerWeight,
                                SearchFunction.combine(
                                    SkyBlockIsland.Slayer::getType,
                                    SlayerModel::getName
                                )
                            )
                        )
                        .withFields(
                            getWeightFields(
                                "Dungeons",
                                dungeonWeight,
                                SearchFunction.combine(
                                    SkyBlockIsland.Dungeon::getType,
                                    DungeonModel::getName
                                )
                            )
                        )
                        .withFields(
                            getWeightFields(
                                "Dungeon Classes",
                                dungeonClassWeight,
                                SearchFunction.combine(
                                    SkyBlockIsland.Dungeon.Class::getType,
                                    DungeonClassModel::getName
                                )
                            )
                        )
                        .build()
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("pets").withEmoji(getEmoji("PETS")).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "pets", "Player Information")
                        .withDescription("Pet Score: **{0}**", member.getPetScore())
                        .build()
                )
                .withItemData(
                    Page.ItemData.builder(SkyBlockIsland.PetInfo.class)
                        .withFieldItems(member.getPets())
                        .withTransformer(stream -> stream
                            .map(petInfo -> FieldItem.builder()
                                .withOption(
                                    SelectMenu.Option.builder()
                                        .withLabel(
                                            "{0}{1}",
                                            petInfo.getPet().map(PetModel::getName).orElse(petInfo.getPrettyName()),
                                            getEmoji(FormatUtil.format("RARITY_{0}", petInfo.getRarity().getKey()))
                                                .map(Emoji::asPreSpacedFormat)
                                                .orElse("")
                                        )
                                        .withEmoji(
                                            skyBlockUser.getSkyBlockEmojis()
                                                .getPetEmoji(petInfo.getName())
                                                .map(Emoji::of)
                                        )
                                        .withValue(petInfo.getPet().map(PetModel::getKey).orElse(petInfo.getName()))
                                        .build()
                                )
                                .withData(FormatUtil.format(
                                    """
                                        {0}Level: **{3}** / **{4}**
                                        {0}Experience:
                                        {1}**{5}**
                                        {2}Progress: **{6}%**
                                        """,
                                    emojiReplyStem,
                                    emojiReplyLine,
                                    emojiReplyEnd,
                                    petInfo.getLevel(),
                                    petInfo.getMaxLevel(),
                                    petInfo.getExperience(),
                                    petInfo.getProgressPercentage()
                                ))
                                .build()
                            )
                        )
                        .withComparators((o1, o2) -> Comparator.comparing(SkyBlockIsland.PetInfo::getRarityOrdinal)
                            .thenComparingInt(SkyBlockIsland.PetInfo::getLevel)
                            .reversed()
                            .thenComparing(SkyBlockIsland.PetInfo::getName)
                            .compare(o1, o2)
                        )
                        .withStyle(PageItem.Style.FIELD_INLINE)
                        .withAmountPerPage(12)
                        .build()
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("accessories").withEmoji(getEmoji("ACCESSORIES")).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "accessories", "Player Information")
                        .withDescription("If you wish to see missing accessory information, use the /missing command.")
                        .build()
                )
                .withItemData(
                    Page.ItemData.builder(AccessoryData.class)
                        .withFieldItems(skyBlockIsland.getPlayerStats(member).getAccessoryBag().getFilteredAccessories())
                        .withTransformer(stream -> stream
                            .map(accessoryData -> FieldItem.builder()
                                .withOption(
                                    SelectMenu.Option.builder()
                                        .withLabel(
                                            "{0}{1}",
                                            accessoryData.getAccessory().getName(),
                                            getEmoji(FormatUtil.format("RARITY_{0}", accessoryData.getRarity().getKey()))
                                                .map(Emoji::asPreSpacedFormat)
                                                .orElse("")
                                        )
                                        .withEmoji(
                                            skyBlockUser.getSkyBlockEmojis()
                                                .getEmoji(accessoryData.getAccessory().getItem().getItemId())
                                                .map(Emoji::of)
                                        )
                                        .withValue(accessoryData.getAccessory().getItem().getItemId())
                                        .build()
                                )
                                .withData(FormatUtil.format(
                                    """
                                        {0}Recombobulator: **{2}**
                                        {1}Enrichment: **{3}**""",
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    getEmoji(accessoryData.isRecombobulated() ? "ACTION_ACCEPT" : "ACTION_DENY")
                                        .map(Emoji::asFormat)
                                        .orElse("?"),
                                    accessoryData.getRarity().isEnrichable() ?
                                        accessoryData.getEnrichment()
                                            .map(AccessoryEnrichmentModel::getStat)
                                            .map(StatModel::getKey)
                                            .map(statKey -> FormatUtil.format("TALISMAN_ENRICHMENT_{0}", statKey))
                                            .flatMap(PlayerCommand::getEmoji)
                                            .or(() -> getEmoji("TAG_NOT_APPLICABLE"))
                                            .map(Emoji::asFormat)
                                            .orElse("N/A") :
                                        getEmoji("TAG_NOT_APPLICABLE").map(Emoji::asFormat).orElse("N/A")
                                ))
                                .build()
                            )
                        )
                        .withComparators(
                            (o1, o2) -> Comparator.comparing(AccessoryData::getRarity)
                                .reversed()
                                .thenComparing(accessoryData -> accessoryData.getAccessory().getName())
                                .compare(o1, o2)
                        )
                        .withStyle(PageItem.Style.FIELD_INLINE)
                        .withAmountPerPage(12)
                        .build()
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("auctions").withEmoji(getEmoji("AUCTIONS")).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "auctions", "Player Information")
                        .withDescription(
                            """
                                {0}Unclaimed Auctions: **{2}**
                                {0}Expired Auctions: **{3}**
                                {1}Total Coins: **{4}**
                                """,
                            emojiReplyStem,
                            emojiReplyEnd,
                            skyBlockUser.getAuctions()
                                .matchAll(SkyBlockAuction::isUnclaimed)
                                .size(),
                            skyBlockUser.getAuctions()
                                .matchAll(skyBlockAuction -> skyBlockAuction.getEndsAt().getRealTime() > System.currentTimeMillis())
                                .size(),
                            skyBlockUser.getAuctions()
                                .stream()
                                .mapToDouble(SkyBlockAuction::getHighestBid)
                                .sum()
                        )
                        .build()
                )
                .withItemData(
                    Page.ItemData.builder(SkyBlockAuction.class)
                        .withFieldItems(skyBlockUser.getAuctions())
                        .withTransformer(stream -> stream
                            .map(skyBlockAuction -> {
                                CompoundTag auctionNbt = skyBlockAuction.getItemNbt().getNbtData();
                                String itemId = auctionNbt.getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY).getValue();

                                return FieldItem.builder()
                                    .withOption(
                                        SelectMenu.Option.builder()
                                            .withLabel(
                                                "{0}{1}",
                                                SimplifiedApi.getRepositoryOf(ItemModel.class)
                                                    .findFirst(ItemModel::getItemId, itemId)
                                                    .map(ItemModel::getName)
                                                    .orElse("Unknown"),
                                                getEmoji(FormatUtil.format("RARITY_{0}", skyBlockAuction.getRarity().getKey()))
                                                    .map(Emoji::asPreSpacedFormat)
                                                    .orElse("")
                                            )
                                            .withEmoji(
                                                skyBlockUser.getSkyBlockEmojis()
                                                    .getEmoji(itemId)
                                                    .map(Emoji::of)
                                            )
                                            .withValue(skyBlockAuction.getAuctionId().toString())
                                            .build()
                                    )
                                    .withData(FormatUtil.format(
                                        """
                                            {0}Starting Bid: **{2}**
                                            {0}Highest Bid: **{3}**
                                            {0}Ends: **{4}**
                                            {1}Highest BIN: **{5}**""",
                                        skyBlockAuction.getStartingBid(),
                                        skyBlockAuction.getHighestBid(),
                                        new DiscordDate(skyBlockAuction.getEndsAt().getRealTime()).asFormat(DiscordDate.Type.RELATIVE),
                                        skyBlockUser.getAuctionHouse()
                                            .getItems()
                                            .stream()
                                            .filter(auctionHouseItem -> auctionHouseItem.getItemNbt()
                                                .getNbtData()
                                                .getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY)
                                                .getValue().equals(itemId)
                                            )
                                            .map(SkyBlockAuction::getHighestBid)
                                            .collect(Concurrent.toList())
                                            .sorted()
                                            .getOrDefault(0, 0L)
                                    ))
                                    .build();
                            })
                        )
                        .withStyle(PageItem.Style.FIELD_INLINE)
                        .withAmountPerPage(12)
                        .build()
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("jacobs_farming").withEmoji(getEmoji("SKILL_FARMING")).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "jacobs_farming", "Player Information")
                        .withFields(
                            Field.builder()
                                .withName("Medals")
                                .withValue(
                                    Arrays.stream(SkyBlockIsland.JacobsFarming.Medal.values())
                                        .flatMap(farmingMedal -> member.getJacobsFarming()
                                            .stream()
                                            .map(jacobsFarming -> FormatUtil.format(
                                                "{0}{1}: {2}",
                                                "",
                                                capitalizeEnum(farmingMedal),
                                                jacobsFarming.getMedals(farmingMedal)
                                            ))
                                        )
                                        .collect(StreamUtil.toStringBuilder(true))
                                        .build()
                                )
                                .isInline()
                                .build(),
                            Field.empty(true),
                            Field.builder()
                                .withName("Upgrades")
                                .withValue(
                                    Arrays.stream(SkyBlockIsland.JacobsFarming.Perk.values())
                                        .flatMap(farmingPerk -> member.getJacobsFarming()
                                            .stream()
                                            .map(jacobsFarming -> FormatUtil.format(
                                                "{0}: {1}",
                                                capitalizeEnum(farmingPerk),
                                                jacobsFarming.getPerk(farmingPerk)
                                            ))
                                        )
                                        .collect(StreamUtil.toStringBuilder(true))
                                        .build()
                                )
                                .isInline()
                                .build()
                        )
                        .withFields(
                            Field.builder()
                                .withName("Collection")
                                .withValue(
                                    SimplifiedApi.getRepositoryOf(CollectionItemModel.class)
                                        .findAll(CollectionItemModel::isFarmingEvent, true)
                                        .stream()
                                        .map(collectionItemModel -> FormatUtil.format(
                                            "{0}",
                                            collectionItemModel.getItem().getName()
                                        ))
                                        .collect(StreamUtil.toStringBuilder(true))
                                        .build()
                                )
                                .isInline()
                                .build(),
                            Field.builder()
                                .withName("Highscores")
                                .withValue(
                                    SimplifiedApi.getRepositoryOf(CollectionItemModel.class)
                                        .findAll(CollectionItemModel::isFarmingEvent, true)
                                        .stream()
                                        .flatMap(collectionItemModel -> member.getJacobsFarming()
                                            .stream()
                                            .map(jacobsFarming -> jacobsFarming.getContests()
                                                .stream()
                                                .filter(farmingContest -> farmingContest.getCollectionName().equals(collectionItemModel.getItem().getItemId()))
                                                .sorted((o1, o2) -> Comparator.comparing(SkyBlockIsland.JacobsFarming.Contest::getCollected).compare(o2, o1))
                                                .map(SkyBlockIsland.JacobsFarming.Contest::getCollected)
                                                .findFirst()
                                                .orElse(0)
                                            )
                                        )
                                        .collect(StreamUtil.toStringBuilder(true))
                                        .build()
                                )
                                .isInline()
                                .build(),
                            Field.builder()
                                .withName("Unique Gold")
                                .withValue(
                                    SimplifiedApi.getRepositoryOf(CollectionItemModel.class)
                                        .findAll(CollectionItemModel::isFarmingEvent, true)
                                        .stream()
                                        .flatMap(collectionItemModel -> member.getJacobsFarming()
                                            .stream()
                                            .map(jacobsFarming -> jacobsFarming.getUniqueGolds()
                                                .stream()
                                                .filter(uniqueGold -> uniqueGold.equals(collectionItemModel))
                                                .findFirst()
                                                .map(farmingCollectionItemModel -> "Yes")
                                                .orElse("No")
                                            )
                                        )
                                        .collect(StreamUtil.toStringBuilder(true))
                                        .build()
                                )
                                .isInline()
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

    private static <T extends SkyBlockIsland.Experience> ConcurrentList<Field> getWeightFields(String title, ConcurrentMap<T, SkyBlockIsland.Experience.Weight> weightMap, Function<T, String> typeNameFunction) {
        return Concurrent.newList(
            Field.builder()
                .withName(title)
                .withValue(
                    weightMap.stream()
                        .map(Map.Entry::getKey)
                        .map(typeNameFunction)
                        .collect(StreamUtil.toStringBuilder(true))
                        .build()
                )
                .isInline()
                .build(),
            Field.builder()
                .withName("Weight")
                .withValue(
                    weightMap.stream()
                        .map(Map.Entry::getValue)
                        .map(SkyBlockIsland.Experience.Weight::getValue)
                        .map(value -> FormatUtil.format("{0}", value))
                        .collect(StreamUtil.toStringBuilder(true))
                        .build()
                )
                .isInline()
                .build(),
            Field.builder()
                .withName("Overflow")
                .withValue(
                    weightMap.stream()
                        .map(Map.Entry::getValue)
                        .map(SkyBlockIsland.Experience.Weight::getOverflow)
                        .map(value -> FormatUtil.format("{0}", value))
                        .collect(StreamUtil.toStringBuilder(true))
                        .build()
                )
                .isInline()
                .build()
        );
    }

    private static SelectMenu.Option.OptionBuilder getOptionBuilder(String identifier) {
        return SelectMenu.Option.builder()
            .withValue(identifier)
            .withLabel(WordUtil.capitalizeFully(identifier.replace("_", " ")));
    }

}
