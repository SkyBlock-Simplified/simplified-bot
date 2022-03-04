package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.mojang.response.MojangProfileResponse;
import dev.sbs.api.data.model.skyblock.collection_items.CollectionItemModel;
import dev.sbs.api.data.model.skyblock.dungeon_classes.DungeonClassModel;
import dev.sbs.api.data.model.skyblock.dungeon_floors.DungeonFloorModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.data.model.skyblock.shop_profile_upgrades.ShopProfileUpgradeModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.StreamUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.regex.Pattern;

@CommandInfo(
    id = "733e6780-84cd-45ed-921a-9b1ca9b02ed6",
    name = "player"
)
public class PlayerCommand extends Command {

    public static final Pattern MOJANG_NAME = Pattern.compile("[\\w]{3,16}");

    protected PlayerCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) {
        return commandContext.reply(
            Response.builder()
                .withReference(commandContext)
                .withPages(
                    Page.builder()
                        .withContent("player command")
                        .build()
                )
                .build()
        );
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<String> getExampleArguments() {
        return Concurrent.newUnmodifiableList(
            "CraftedFury",
            "CraftedFury Pineapple"
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
                (argument, commandContext) -> StringUtil.isUUID(argument) || MOJANG_NAME.matcher(argument).matches()
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

    public static Page buildPage(MojangProfileResponse mojangProfile, SkyBlockIsland skyBlockIsland, SkyBlockIsland.Member member, String identifier) {
        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");

        return switch (identifier.toLowerCase()) {
            case "stats" -> Page.builder()
                .withOption(
                    getOptionBuilder("stats", identifier)
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "stats")
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
                    getOptionBuilder("skills", identifier)
                        .build()
                )
                .withEmbeds(
                    getSkillEmbed(
                        mojangProfile,
                        skyBlockIsland,
                        "skills",
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
                    getOptionBuilder("slayers", identifier)
                        .build()
                )
                .withEmbeds(
                    getSkillEmbed(
                        mojangProfile,
                        skyBlockIsland,
                        "slayers",
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
                    getOptionBuilder("dungeons", identifier)
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, "dungeons")
                        .withField(
                            "Details",
                            FormatUtil.format(
                                """
                                {0}Select Class: {2}
                                {0}Average Level: {3,number,#.##}
                                {0}Total Experience: {4,number,#.##}
                                {1}Total Progress: {5,number,#.##}%
                                """,
                                emojiReplyStem,
                                emojiReplyEnd,
                                member.getDungeons().getSelectedClassModel().map(DungeonClassModel::getName).orElse(getEmojiAsFormat("TEXT_NULL", "*<null>*")),
                                0,
                                0,
                                0
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
                                "todo"
                            ),
                            Field.of(
                                "Best Time",
                                "todo"
                            )
                        )
                        .build()
                )
                .build();
            case "dungeon_classes" -> Page.builder()
                .withOption(
                    getOptionBuilder("dungeon_classes", identifier)
                        .build()
                )
                .withEmbeds(
                    getSkillEmbed(
                        mojangProfile,
                        skyBlockIsland,
                        "dungeon_classes",
                        member.getDungeons().getClasses(),
                        member.getDungeonClassAverage(),
                        member.getDungeonClassExperience(),
                        member.getDungeonClassProgressPercentage(),
                        dungeonClass -> dungeonClass.getType().getName()
                    )
                )
                .build();
            case "jacobs_farming" -> Page.builder()
                .withOption(
                    getOptionBuilder("jacobs_farming", identifier)
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, identifier)
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
                                "Upgrades", // TODO: Database
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
        return getEmbedBuilder(mojangProfile, skyBlockIsland, value)
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

    private static SelectMenu.Option.OptionBuilder getOptionBuilder(String value, String identifier) {
        return SelectMenu.Option.builder()
            .withValue(value)
            .withLabel(WordUtil.capitalizeFully(value.replace("_", " ")))
            .isDefault(value.equalsIgnoreCase(identifier));
    }

    private static Embed.EmbedBuilder getEmbedBuilder(MojangProfileResponse mojangProfile, SkyBlockIsland skyBlockIsland, String identifier) {
        return Embed.builder()
            .withAuthor("Player Information")
            .withColor(Color.DARK_GRAY)
            .withTitle(
                "{0} :: {1} ({2}{3})",
                WordUtil.capitalizeFully(identifier.replace("_", " ")),
                mojangProfile.getUsername(),
                skyBlockIsland.getProfileName()
                    .map(ProfileModel::getEmoji)
                    .flatMap(Emoji::of)
                    .map(emoji -> FormatUtil.format("{0} ", emoji.asFormat()))
                    .orElse(""),
                skyBlockIsland.getProfileName().map(ProfileModel::getName).orElse("")
            )
            .withTimestamp(Instant.now())
            .withThumbnailUrl(
                "https://api.sbs.dev/mojang/avatar/{0}",
                mojangProfile.getUsername()
            );
    }

}
