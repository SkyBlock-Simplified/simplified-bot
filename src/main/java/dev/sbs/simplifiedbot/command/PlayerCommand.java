package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.SkyBlockAuction;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.account.Banking;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.Dungeon;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.JacobsFarming;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.Skill;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.SkyBlockIsland;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.Slayer;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.pets.Pet;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.util.Experience;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.playerstats.data.AccessoryData;
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
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.context.interaction.deferrable.application.slash.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.handler.item.CollectionItemHandler;
import dev.sbs.discordapi.response.page.handler.item.ItemHandler;
import dev.sbs.discordapi.response.page.item.FieldItem;
import dev.sbs.discordapi.response.page.item.Item;
import dev.sbs.discordapi.util.DiscordDate;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

@CommandId("733e6780-84cd-45ed-921a-9b1ca9b02ed6")
public class PlayerCommand extends SkyBlockUserCommand {

    protected PlayerCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull SlashCommandContext commandContext, @NotNull SkyBlockUser skyBlockUser) {
        return commandContext.reply(
            Response.builder()
                .isInteractable()
                .replyMention()
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
        Experience.Weight totalWeight = member.getTotalWeight();
        ConcurrentMap<Skill, Experience.Weight> skillWeight = member.getSkillWeight();
        ConcurrentMap<Slayer, Experience.Weight> slayerWeight = member.getSlayerWeight();
        ConcurrentMap<Dungeon, Experience.Weight> dungeonWeight = member.getDungeonWeight();
        ConcurrentMap<Dungeon.Class, Experience.Weight> dungeonClassWeight = member.getDungeonClassWeight();

        return Concurrent.newList(
            Page.builder()
                .withOption(getOptionBuilder("stats").withEmoji(Emoji.of(skyBlockIsland.getProfileModel().map(ProfileModel::getEmoji).get())).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "stats")
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
                                    skyBlockIsland.getBanking().map(Banking::getBalance).orElse(0.0),
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
                                                    .map(shopProfileUpgradeModel -> String.format(
                                                        "%s: %s / %s",
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
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "weight")
                        .withDescription(String.format(
                            """
                            %1$sTotal Weight: **%3$s** (**%4$s** + **%5$s**)
                            %1$sTotal Skill Weight: **%6$s** (**%7$s** + **%8$s**)
                            %1$sTotal Slayer Weight: **%9$s** (**%10$s** + **%11$s**)
                            %1$sTotal Dungeon Weight: **%12$s** (**%13$s** + **%14$s**)
                            %2$sTotal Dungeon Class Weight: **%15$s** (**%16$s** + **%17$s**)
                            """,
                            emojiReplyStem,
                            emojiReplyEnd,
                            totalWeight.getTotal(),
                            totalWeight.getValue(),
                            totalWeight.getOverflow(),
                            skillWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getTotal).sum(),
                            skillWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getValue).sum(),
                            skillWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getOverflow).sum(),
                            slayerWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getTotal).sum(),
                            slayerWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getValue).sum(),
                            slayerWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getOverflow).sum(),
                            dungeonWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getTotal).sum(),
                            dungeonWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getValue).sum(),
                            dungeonWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getValue).sum(),
                            dungeonClassWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getTotal).sum(),
                            dungeonClassWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getValue).sum(),
                            dungeonClassWeight.stream().map(Map.Entry::getValue).mapToDouble(Experience.Weight::getOverflow).sum()
                        ))
                        .withFields(
                            getWeightFields(
                                "Skills",
                                skillWeight,
                                SearchFunction.combine(
                                    Skill::getType,
                                    SkillModel::getName
                                )
                            )
                        )
                        .withFields(
                            getWeightFields(
                                "Slayers",
                                slayerWeight,
                                SearchFunction.combine(
                                    Slayer::getType,
                                    SlayerModel::getName
                                )
                            )
                        )
                        .withFields(
                            getWeightFields(
                                "Dungeons",
                                dungeonWeight,
                                SearchFunction.combine(
                                    Dungeon::getType,
                                    DungeonModel::getName
                                )
                            )
                        )
                        .withFields(
                            getWeightFields(
                                "Dungeon Classes",
                                dungeonClassWeight,
                                SearchFunction.combine(
                                    Dungeon.Class::getType,
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
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "pets")
                        .withDescription(String.format(
                            """
                            Unique Pets: **%s** / **%s**
                            Pet Score: **%s** (+%s Magic Find)
                            """,
                            member.getPetData()
                                .getPets()
                                .stream()
                                .filter(StreamUtil.distinctByKey(Pet::getName))
                                .collect(Concurrent.toList())
                                .size(),
                            SimplifiedApi.getRepositoryOf(PetModel.class)
                                .findAll()
                                .size(),
                            member.getPetData().getPetScore(),
                            member.getPetData().getPetScoreMagicFind()
                        ))
                        .build()
                )
                .withItemHandler(
                    CollectionItemHandler.builder(Pet.class)
                        .withItems(member.getPetData().getPets())
                        .withTransformer(stream -> stream
                            .map(pet -> FieldItem.builder()
                                .withOption(
                                    SelectMenu.Option.builder()
                                        .withLabel(
                                            "%s%s",
                                            pet.getPet().map(PetModel::getName).orElse(pet.getPrettyName()),
                                            getEmoji(String.format("RARITY_%s", pet.getRarity().getKey()))
                                                .map(Emoji::asPreSpacedFormat)
                                                .orElse("")
                                        )
                                        .withEmoji(
                                            skyBlockUser.getSkyBlockEmojis()
                                                .getPetEmoji(pet.getName())
                                                .map(Emoji::of)
                                        )
                                        .withValue(pet.getPet().map(PetModel::getKey).orElse(pet.getName()))
                                        .build()
                                )
                                .withData(String.format(
                                    """
                                        %1$sLevel: **%4$d** / **%5$d**
                                        %1$sExperience:
                                        %2$s**%6$f**
                                        %3$sProgress: **%7$f%%**
                                        """,
                                    emojiReplyStem,
                                    emojiReplyLine,
                                    emojiReplyEnd,
                                    pet.getLevel(),
                                    pet.getMaxLevel(),
                                    pet.getExperience(),
                                    pet.getProgressPercentage()
                                ))
                                .build()
                            )
                        )
                        .withSorters(
                            ItemHandler.Sorter.<Pet>builder()
                                .withComparators((o1, o2) -> Comparator.comparing(Pet::getRarityOrdinal)
                                    .thenComparingInt(Pet::getLevel)
                                    .reversed()
                                    .thenComparing(Pet::getName)
                                    .compare(o1, o2)
                                )
                                .withLabel("Default")
                                .build()
                        )
                        .withStyle(Item.Style.FIELD_INLINE)
                        .withAmountPerPage(12)
                        .build()
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("accessories").withEmoji(getEmoji("ACCESSORIES")).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "accessories")
                        .withDescription("If you wish to see missing accessory information, use the /missing command.")
                        .build()
                )
                .withItemHandler(
                    CollectionItemHandler.builder(AccessoryData.class)
                        .withItems(skyBlockIsland.getPlayerStats(member).getAccessoryBag().getFilteredAccessories())
                        .withTransformer(stream -> stream
                            .map(accessoryData -> FieldItem.builder()
                                .withOption(
                                    SelectMenu.Option.builder()
                                        .withLabel(
                                            "{0}{1}",
                                            accessoryData.getAccessory().getName(),
                                            getEmoji(String.format("RARITY_%s", accessoryData.getRarity().getKey()))
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
                                    (accessoryData.getRarity().isEnrichable() ? accessoryData.getEnrichment()
                                        .map(AccessoryEnrichmentModel::getStat)
                                        .map(StatModel::getKey)
                                        .map(statKey -> String.format("TALISMAN_ENRICHMENT_%s", statKey))
                                        .flatMap(PlayerCommand::getEmoji)
                                        .or(() -> getEmoji("TAG_NOT_APPLICABLE")) :
                                        getEmoji("TAG_NOT_APPLICABLE"))
                                        .map(Emoji::asFormat)
                                        .orElse("N/A")
                                ))
                                .build()
                            )
                        )
                        .withSorters(
                            ItemHandler.Sorter.<AccessoryData>builder()
                                .withComparators((o1, o2) -> Comparator.comparing(AccessoryData::getRarity)
                                    .reversed()
                                    .thenComparing(accessoryData -> accessoryData.getAccessory().getName())
                                    .compare(o1, o2)
                                )
                                .withLabel("Default")
                                .build()
                        )
                        .withStyle(Item.Style.FIELD_INLINE)
                        .withAmountPerPage(12)
                        .build()
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("auctions").withEmoji(getEmoji("AUCTIONS")).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "auctions")
                        .withDescription(String.format(
                            """
                            %1$sUnclaimed Auctions: **%3$s**
                            %1$sExpired Auctions: **%4$s**
                            %2$sTotal Coins: **%5$s**
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
                        ))
                        .build()
                )
                .withItemHandler(
                    CollectionItemHandler.builder(SkyBlockAuction.class)
                        .withItems(skyBlockUser.getAuctions())
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
                                                getEmoji(String.format("RARITY_%s", skyBlockAuction.getRarity().getKey()))
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
                        .withStyle(Item.Style.FIELD_INLINE)
                        .withAmountPerPage(12)
                        .build()
                )
                .build(),
            Page.builder()
                .withOption(getOptionBuilder("jacobs_farming").withEmoji(getEmoji("SKILL_FARMING")).build())
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "jacobs_farming")
                        .withFields(
                            Field.builder()
                                .withName("Medals")
                                .withValue(
                                    Arrays.stream(JacobsFarming.Medal.values())
                                        .map(farmingMedal -> String.format(
                                            "%s%s: %s",
                                            "",
                                            capitalizeEnum(farmingMedal),
                                            member.getJacobsFarming().getMedals(farmingMedal)
                                        ))
                                        .collect(StreamUtil.toStringBuilder(true))
                                        .build()
                                )
                                .isInline()
                                .build(),
                            Field.empty(true),
                            Field.builder()
                                .withName("Upgrades")
                                .withValue(
                                    Arrays.stream(JacobsFarming.Perk.values())
                                        .map(farmingPerk -> String.format(
                                            "%s: %s",
                                            capitalizeEnum(farmingPerk),
                                            member.getJacobsFarming().getPerk(farmingPerk)
                                        ))
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
                                        .map(collectionItemModel -> collectionItemModel.getItem().getName())
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
                                        .map(collectionItemModel -> member.getJacobsFarming()
                                            .getContests()
                                            .stream()
                                            .filter(farmingContest -> farmingContest.getCollectionName().equals(collectionItemModel.getItem().getItemId()))
                                            .sorted((o1, o2) -> Comparator.comparing(JacobsFarming.Contest::getCollected).compare(o2, o1))
                                            .map(JacobsFarming.Contest::getCollected)
                                            .findFirst()
                                            .orElse(0)
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
                                        .map(collectionItemModel -> member.getJacobsFarming().getUniqueGolds()
                                            .stream()
                                            .filter(uniqueGold -> uniqueGold.equals(collectionItemModel))
                                            .findFirst()
                                            .map(farmingCollectionItemModel -> "Yes")
                                            .orElse("No")
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

    private static <T extends Experience> ConcurrentList<Field> getWeightFields(String title, ConcurrentMap<T, Experience.Weight> weightMap, Function<T, String> typeNameFunction) {
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
                        .map(Experience.Weight::getValue)
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
                        .map(Experience.Weight::getOverflow)
                        .collect(StreamUtil.toStringBuilder(true))
                        .build()
                )
                .isInline()
                .build()
        );
    }

    private static SelectMenu.Option.Builder getOptionBuilder(String identifier) {
        return SelectMenu.Option.builder()
            .withValue(identifier)
            .withLabel(StringUtil.capitalizeFully(identifier.replace("_", " ")));
    }

}
