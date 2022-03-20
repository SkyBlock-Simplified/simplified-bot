package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.skyblock.collection_items.CollectionItemModel;
import dev.sbs.api.data.model.skyblock.dungeon_classes.DungeonClassModel;
import dev.sbs.api.data.model.skyblock.dungeon_floors.DungeonFloorModel;
import dev.sbs.api.data.model.skyblock.dungeons.DungeonModel;
import dev.sbs.api.data.model.skyblock.shop_profile_upgrades.ShopProfileUpgradeModel;
import dev.sbs.api.data.model.skyblock.skills.SkillModel;
import dev.sbs.api.data.model.skyblock.slayers.SlayerModel;
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
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.PageItem;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@CommandInfo(
    id = "733e6780-84cd-45ed-921a-9b1ca9b02ed6",
    name = "player"
)
public class PlayerCommand extends SkyBlockUserCommand {

    private static final ConcurrentList<String> PAGE_IDENTIFIERS = Concurrent.newUnmodifiableList("stats", "skills", "skills", "slayers", "dungeons", "dungeon_classes", "weight", "jacobs_farming");

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
                .withPages(
                    getPages(
                        skyBlockUser,
                        "stats"
                    )
                )
                .build()
        );
    }

    public static @NotNull ConcurrentList<Page> getPages(@NotNull SkyBlockUser skyBlockUser, @NotNull String requestingIdentifier) {
        ConcurrentList<String> remainingIdentifiers = Concurrent.newList(PAGE_IDENTIFIERS);
        remainingIdentifiers.remove(requestingIdentifier);
        ConcurrentList<Page> pages = Concurrent.newList(buildPage(skyBlockUser, requestingIdentifier, requestingIdentifier));

        pages.addAll(
            remainingIdentifiers.stream()
                .map(identifier -> buildPage(skyBlockUser, identifier, requestingIdentifier))
                .collect(Concurrent.toList())
        );

        return pages;
    }

    public static @NotNull Page buildPage(@NotNull SkyBlockUser skyBlockUser, @NotNull String identifier, @NotNull String requestingIdentifier) {
        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        MojangProfileResponse mojangProfile = skyBlockUser.getMojangProfile();
        SkyBlockIsland skyBlockIsland = skyBlockUser.getSelectedIsland();
        SkyBlockIsland.Member member = skyBlockUser.getMember();

        // Weights
        SkyBlockIsland.Experience.Weight totalWeight = member.getTotalWeight();
        ConcurrentMap<SkyBlockIsland.Skill, SkyBlockIsland.Experience.Weight> skillWeight = member.getSkillWeight();
        ConcurrentMap<SkyBlockIsland.Slayer, SkyBlockIsland.Experience.Weight> slayerWeight = member.getSlayerWeight();
        ConcurrentMap<SkyBlockIsland.Dungeon, SkyBlockIsland.Experience.Weight> dungeonWeight = member.getDungeonWeight();
        ConcurrentMap<SkyBlockIsland.Dungeon.Class, SkyBlockIsland.Experience.Weight> dungeonClassWeight = member.getDungeonClassWeight();

        return switch (identifier.toLowerCase()) {
            case "stats" -> Page.builder()
                .withOption(
                    getOptionBuilder(identifier, requestingIdentifier)
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, identifier, "Player Information")
                        .withFields(
                            Field.of(
                                FormatUtil.format(
                                    "{0} Details",
                                    getEmoji("STATUS_INFO")
                                ),
                                FormatUtil.format(
                                    """
                                        {0}Status: {2}
                                        {0}Deaths: {3}
                                        {1}Guild: {4}
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    "?",
                                    member.getDeathCount(),
                                    "?"
                                ),
                                true
                            ),
                            Field.of(
                                FormatUtil.format(
                                    "{0} Coins",
                                    getEmoji("TRADING_COIN_PIGGY")
                                ),
                                FormatUtil.format(
                                    """
                                        {0}Bank: {2}
                                        {1}Purse: {3}
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    skyBlockIsland.getBanking().map(SkyBlockIsland.Banking::getBalance).orElse(0.0),
                                    member.getPurse()
                                ),
                                true
                            )
                        )
                        .withFields(
                            Field.of(
                                FormatUtil.format(
                                    "{0} Community Upgrades",
                                    getEmoji("GEM_EMERALD")
                                ),
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
                                ),
                                true
                            ),
                            Field.of(
                                FormatUtil.format(
                                    "{0} Minions",
                                    getEmoji("LAPIS_MINION")
                                ),
                                FormatUtil.format(
                                    """
                                        {0}Slots: {2}
                                        {1}Uniques: {3}
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    skyBlockIsland.getCommunityUpgrades()
                                        .stream()
                                        .mapToInt(communityUpgrades -> SimplifiedApi.getRepositoryOf(ShopProfileUpgradeModel.class)
                                            .findFirst(ShopProfileUpgradeModel::getKey, "MINION_SLOTS")
                                            .map(communityUpgrades::getHighestTier)
                                            .orElse(0)
                                        )
                                        .sum(),
                                    member.getMinions()
                                        .stream()
                                        .mapToInt(minion -> minion.getUnlocked().size())
                                        .sum()
                                ),
                                true
                            )
                        )
                        .build()
                )
                .build();
            case "skills" -> Page.builder()
                .withOption(
                    getOptionBuilder(identifier, requestingIdentifier)
                        .build()
                )
                .withEmbeds(
                    getSkillEmbed(
                        mojangProfile,
                        skyBlockIsland,
                        identifier,
                        member.getSkills(),
                        member.getSkillAverage(),
                        member.getSkillExperience(),
                        member.getSkillProgressPercentage(),
                        skill -> skill.getType().getName()
                    )
                )
                .build();
            case "slayers" -> Page.builder()
                .withOption(
                    getOptionBuilder(identifier, requestingIdentifier)
                        .build()
                )
                .withEmbeds(
                    getSkillEmbed(
                        mojangProfile,
                        skyBlockIsland,
                        identifier,
                        member.getSlayers(),
                        member.getSlayerAverage(),
                        member.getSlayerExperience(),
                        member.getSlayerProgressPercentage(),
                        slayer -> slayer.getType().getName()
                    )
                )
                .build();
            case "dungeons" -> Page.builder()
                .withOption(
                    getOptionBuilder(identifier, requestingIdentifier)
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, identifier, "Player Information")
                        .withField(
                            "Details",
                            FormatUtil.format(
                                """
                                {0}Select Class: {2}
                                {0}Average Class Level: {3,number,#.##}
                                {0}Total Class Experience: {4,number,#.##}
                                {1}Total Class Progress: {5,number,#.##}%
                                """,
                                emojiReplyStem,
                                emojiReplyEnd,
                                member.getDungeons().getSelectedClassModel().map(DungeonClassModel::getName).orElse(getEmojiAsFormat("TEXT_NULL", "*<null>*")),
                                member.getDungeonClassAverage(),
                                member.getDungeonClassExperience(),
                                member.getDungeonClassProgressPercentage()
                            )
                        )
                        .withFields(
                            Field.of(
                                "Floor",
                                StringUtil.join(
                                    SimplifiedApi.getRepositoryOf(DungeonFloorModel.class)
                                        .stream()
                                        .map(dungeonFloorModel -> dungeonFloorModel.getFloor() > 0 ? FormatUtil.format("Floor {0}", dungeonFloorModel.getFloor()) : "Entrance")
                                )
                            ),
                            Field.of(
                                "Best Score",
                                SimplifiedApi.getRepositoryOf(DungeonFloorModel.class)
                                    .stream()
                                    .map(dungeonFloorModel -> member.getDungeons()
                                        .getDungeon(SimplifiedApi.getRepositoryOf(DungeonModel.class).findFirstOrNull(DungeonModel::getKey, "CATACOMBS"))
                                        .getBestScore(dungeonFloorModel)
                                    )
                                    .map(value -> FormatUtil.format("{0}", value))
                                    .collect(StreamUtil.toStringBuilder(true))
                                    .build()
                            ),
                            Field.of(
                                "Best Time",
                                SimplifiedApi.getRepositoryOf(DungeonFloorModel.class)
                                    .stream()
                                    .map(dungeonFloorModel -> Optional.ofNullable(
                                        member.getDungeons()
                                            .getDungeon(SimplifiedApi.getRepositoryOf(DungeonModel.class).findFirstOrNull(DungeonModel::getKey, "CATACOMBS"))
                                            .getBestRuns(dungeonFloorModel)
                                            .sort(SkyBlockIsland.Dungeon.Run::getElapsedTime)
                                            .getOrDefault(0, null)
                                    ).map(SkyBlockIsland.Dungeon.Run::getElapsedTime).orElse(0))
                                    .map(value -> FormatUtil.format("{0}", value))
                                    .collect(StreamUtil.toStringBuilder(true))
                                    .build()
                            )
                        )
                        .build()
                )
                .build();
            case "dungeon_classes" -> Page.builder()
                .withOption(
                    getOptionBuilder(identifier, requestingIdentifier)
                        .build()
                )
                .withEmbeds(
                    getSkillEmbed(
                        mojangProfile,
                        skyBlockIsland,
                        identifier,
                        member.getDungeons().getClasses(),
                        member.getDungeonClassAverage(),
                        member.getDungeonClassExperience(),
                        member.getDungeonClassProgressPercentage(),
                        dungeonClass -> dungeonClass.getType().getName()
                    )
                )
                .build();
            case "weight" -> Page.builder()
                .withOption(
                    getOptionBuilder(identifier, requestingIdentifier)
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, identifier, "Player Information")
                        .withDescription(
                            """
                                {0}Total Weight: **{2}** (**{3}** with Overflow)
                                {0}Total Skill Weight: **{4}** (**{5}** with Overflow)
                                {0}Total Slayer Weight: **{6}** (**{7}** with Overflow)
                                {0}Total Dungeon Weight: **{8}** (**{9}** with Overflow)
                                {1}Total Dungeon Class Weight: **{10}** (**{11}** with Overflow)""",
                            emojiReplyStem,
                            emojiReplyEnd,
                            totalWeight.getValue(),
                            totalWeight.getOverflow(),
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
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getValue)
                                .sum(),
                            slayerWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getOverflow)
                                .sum(),
                            dungeonWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getValue)
                                .sum(),
                            dungeonWeight.stream()
                                .map(Map.Entry::getValue)
                                .mapToDouble(SkyBlockIsland.Experience.Weight::getOverflow)
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
                .build();
            case "pets" -> Page.builder()
                .withPageItemStyle(PageItem.Style.FIELD_INLINE)
                .withItemsPerPage(12)
                .withOption(
                    getOptionBuilder(identifier, requestingIdentifier)
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, identifier, "Player Information")
                        .withDescription("Pet Score: **{0}**", member.getPetScore())
                        .build()
                )
                .withItems(
                    member.getPets()
                        .sort(petInfo -> petInfo.getRarity().getOrdinal(), SkyBlockIsland.PetInfo::getLevel)
                        .stream()
                        .map(petInfo -> PageItem.builder()
                            .withOption(
                                SelectMenu.Option.builder()
                                    .withLabel(FormatUtil.format(
                                        "{0}{1}{2}",
                                        skyBlockUser.getSkyBlockEmojis().getPetEmoji(petInfo.getName()),
                                        petInfo.getPet().getName(),
                                        getEmoji(FormatUtil.format("RARITY_{0}", petInfo.getRarity().getKey())).map(Emoji::asPreSpacedFormat).orElse("")
                                    ))
                                    .withValue(petInfo.getPet().getKey())
                                    .build()
                            )
                            .withData(FormatUtil.format(
                                """
                                    {0}Level: **{2}** / **{3}**
                                    {0}Experience: **{4}**
                                    {0}Progress: **{5}%**
                                    """,
                                petInfo.getLevel(),
                                petInfo.getMaxLevel(),
                                petInfo.getExperience(),
                                petInfo.getProgressPercentage()
                            ))
                            .build()
                        )
                        .collect(Concurrent.toList())
                )
                .build();
            case "jacobs_farming" -> Page.builder()
                .withOption(
                    getOptionBuilder(identifier, requestingIdentifier)
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, identifier, "Player Information")
                        .withFields(
                            Field.of(
                                "Medals",
                                StringUtil.join(
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
                                        .collect(Concurrent.toList()),
                                    "\n"
                                )
                            ),
                            Field.empty(true),
                            Field.of(
                                "Upgrades",
                                StringUtil.join(
                                    Arrays.stream(SkyBlockIsland.JacobsFarming.Perk.values())
                                        .flatMap(farmingPerk -> member.getJacobsFarming()
                                            .stream()
                                            .map(jacobsFarming -> FormatUtil.format(
                                                "{0}: {1}",
                                                capitalizeEnum(farmingPerk),
                                                jacobsFarming.getPerk(farmingPerk)
                                            ))
                                        )
                                        .collect(Concurrent.toList()),
                                    "\n"
                                )
                            )
                        )
                        .withField(
                            "Collection",
                            StringUtil.join(
                                SimplifiedApi.getRepositoryOf(CollectionItemModel.class)
                                    .findAll(CollectionItemModel::isFarmingEvent, true)
                                    .stream()
                                    .map(collectionItemModel -> FormatUtil.format(
                                        "{0}",
                                        collectionItemModel.getItem().getName()
                                    ))
                                    .collect(Concurrent.toList()),
                                "\n"
                            )
                        )
                        .withField(
                            "Highscores",
                            StringUtil.join(
                                SimplifiedApi.getRepositoryOf(CollectionItemModel.class)
                                    .findAll(CollectionItemModel::isFarmingEvent, true)
                                    .stream()
                                    .flatMap(collectionItemModel -> member.getJacobsFarming()
                                        .stream()
                                        .flatMap(jacobsFarming -> jacobsFarming.getContests()
                                            .stream()
                                            .filter(farmingContest -> farmingContest.getCollectionName().equals(collectionItemModel.getItem().getItemId()))
                                            .sorted((o1, o2) -> Comparator.comparing(SkyBlockIsland.JacobsFarming.Contest::getCollected).compare(o1, o2))
                                            .map(farmingContest -> String.valueOf(farmingContest.getCollected()))
                                        )
                                    )
                                    .collect(Concurrent.toList()),
                                "\n"
                            )
                        )
                        .withField(
                            "Unique Gold",
                            StringUtil.join(
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
                                    .collect(Concurrent.toList()),
                                "\n"
                            )
                        )
                        .build()
                )
                .build();
            default -> Page.builder().build(); // Will never create this
        };
    }

    private static <T extends SkyBlockIsland.Experience> ConcurrentList<Field> getWeightFields(String title, ConcurrentMap<T, SkyBlockIsland.Experience.Weight> weightMap, Function<T, String> typeNameFunction) {
        return Concurrent.newList(
            Field.of(
                title,
                weightMap.stream()
                    .map(Map.Entry::getKey)
                    .map(typeNameFunction)
                    .collect(StreamUtil.toStringBuilder(true))
                    .build(),
                true
            ),
            Field.of(
                "Weight",
                weightMap.stream()
                    .map(Map.Entry::getValue)
                    .map(SkyBlockIsland.Experience.Weight::getValue)
                    .map(value -> FormatUtil.format("{0}", value))
                    .collect(StreamUtil.toStringBuilder(true))
                    .build(),
                true
            ),
            Field.of(
                "Overflow",
                weightMap.stream()
                    .map(Map.Entry::getValue)
                    .map(SkyBlockIsland.Experience.Weight::getOverflow)
                    .map(value -> FormatUtil.format("{0}", value))
                    .collect(StreamUtil.toStringBuilder(true))
                    .build(),
                true
            )
        );
    }

    private static <T extends SkyBlockIsland.Experience> Embed getSkillEmbed(
        MojangProfileResponse mojangProfile,
        SkyBlockIsland skyBlockIsland,
        String value,
        ConcurrentList<T> experienceObjects,
        double average,
        double experience,
        double totalProgress,
        Function<T, String> nameFunction
    ) {
        return getEmbedBuilder(mojangProfile, skyBlockIsland, value, "Player Information")
                .withField(
                    "Details",
                    FormatUtil.format(
                        """
                        {0}Average Level: {2,number,#.##}
                        {0}Total Experience: {3,number,#.##}
                        {1}Total Progress: {4,number,#.##}%
                        """,
                        average,
                        experience,
                        totalProgress
                    )
                )
                .withFields(
                    Field.of(
                        WordUtil.capitalizeFully(value.replace("_", " ")),
                        StringUtil.join(
                            experienceObjects.stream()
                                .map(nameFunction)
                                .collect(Concurrent.toList()),
                            "\n"
                        )
                    ),
                    Field.of(
                        "Level (Progress)",
                        StringUtil.join(
                            experienceObjects.stream()
                                .map(expObject -> FormatUtil.format(
                                    "{0} ({1,number,#.##}%)",
                                    expObject.getLevel(),
                                    expObject.getTotalProgressPercentage()
                                ))
                                .collect(Concurrent.toList()),
                            "\n"
                        )
                    ),
                    Field.of(
                        "Experience",
                        StringUtil.join(
                            experienceObjects.stream()
                                .map(expObject -> FormatUtil.format(
                                    "{0,number}",
                                    expObject.getExperience()
                                ))
                                .collect(Concurrent.toList()),
                            "\n"
                        )
                    )
                )
                .build();
    }

    private static SelectMenu.Option.OptionBuilder getOptionBuilder(String identifier, String requestingIdentifier) {
        return SelectMenu.Option.builder()
            .withValue(identifier)
            .withLabel(WordUtil.capitalizeFully(identifier.replace("_", " ")))
            .isDefault(identifier.equalsIgnoreCase(requestingIdentifier));
    }

}
