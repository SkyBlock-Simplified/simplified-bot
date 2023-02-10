package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.request.HypixelPlayerRequest;
import dev.sbs.api.client.hypixel.request.HypixelSkyBlockRequest;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelPlayerResponse;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.Skill;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.SkyBlockIsland;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.util.Experience;
import dev.sbs.api.data.model.Model;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeon_classes.DungeonClassModel;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeons.DungeonModel;
import dev.sbs.api.data.model.skyblock.skills.SkillModel;
import dev.sbs.api.data.model.skyblock.slayers.SlayerModel;
import dev.sbs.api.minecraft.text.MinecraftChatFormatting;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.collection.sort.SortOrder;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.data.tuple.Triple;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.StreamUtil;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.item.FieldItem;
import dev.sbs.discordapi.response.page.item.PageItem;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.UUID;

@CommandInfo(
    id = "b04d133d-3532-447b-8782-37d1036f3957",
    name = "guild"
)
public class GuildCommand extends Command {

    protected GuildCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final ConcurrentList<String> emojiStrings = new ConcurrentList<>();

    private static final ConcurrentList<String> pageIdentifiers = new ConcurrentList<>(
        "general_information",
        "skill_averages",
        "slayer_information",
        "dungeon_information",
        "weight_leaderboard",
        "networth_leaderboard"
    );

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        String guildName = commandContext.getArgument("name").getValue().orElseThrow();

        HypixelGuildResponse hypixelGuildResponse = SimplifiedApi.getWebApi(HypixelPlayerRequest.class).getGuildByName(guildName);

        if (hypixelGuildResponse.getGuild().isEmpty()) {
            return commandContext.reply(
                Response.builder()
                    .withPages(
                        Page.builder()
                            .withEmbeds(
                                Embed.builder()
                                    .withTitle("Command Error")
                                    .withDescription("Invalid guild's name: " + guildName)
                                    .withColor(Color.RED)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );
        }

        ConcurrentList<SkillModel> skillModels = SimplifiedApi.getRepositoryOf(SkillModel.class).findAll();
        ConcurrentList<SlayerModel> slayerModels = SimplifiedApi.getRepositoryOf(SlayerModel.class).findAll().sorted(SortOrder.ASCENDING, Model::getId);
        DungeonModel catacombs = SimplifiedApi.getRepositoryOf(DungeonModel.class).findFirstOrNull(DungeonModel::getKey, "CATACOMBS");
        ConcurrentList<DungeonClassModel> dungeonClassModels = SimplifiedApi.getRepositoryOf(DungeonClassModel.class).findAll().sorted(SortOrder.ASCENDING, Model::getId);
        HypixelSkyBlockRequest skyBlockData = SimplifiedApi.getWebApi(HypixelSkyBlockRequest.class);
        HypixelGuildResponse.Guild guild = hypixelGuildResponse.getGuild().get();
        HypixelPlayerRequest hypixelPlayerRequest = SimplifiedApi.getWebApi(HypixelPlayerRequest.class);

        ConcurrentMap<HypixelPlayerResponse.Player, SkyBlockIsland> guildMembers = guild.getMembers().stream()
            .limit(2) //TODO: limiting size!!
            .map(member -> Pair.of(
                hypixelPlayerRequest.getPlayer(member.getUniqueId()).getPlayer(),
                skyBlockData.getProfiles(member.getUniqueId()).getLastPlayed())
            )
            .collect(Concurrent.toMap());

        ConcurrentMap<UUID, String> ignMap = guildMembers.keySet().stream()
            .map(key -> Pair.of(key.getUniqueId(), key.getDisplayName()))
            .collect(Concurrent.toMap());

        ConcurrentList<SkyBlockIsland.Member> guildMemberPlayers = guildMembers.keySet().stream()
            .map(hypixelPlayer -> guildMembers.get(hypixelPlayer).getMember(hypixelPlayer.getUniqueId()).orElseThrow())
            .collect(Concurrent.toList());

        ConcurrentMap<SkyBlockIsland.Member, Experience.Weight> totalWeights = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getTotalWeight()))
            .collect(Concurrent.toMap());

        ConcurrentMap<SkyBlockIsland.Member, Long> networths = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getPurse())) //TODO: networth query
            .collect(Concurrent.toMap());

        ConcurrentMap<SkyBlockIsland.Member, ConcurrentList<Skill>> skills = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getSkills()))
            .collect(Concurrent.toMap());

        ConcurrentMap<String, Emoji> emojis = new ConcurrentMap<>();
        skillModels.forEach(skillModel -> emojis.put(skillModel.getKey(), Emoji.of(skillModel.getEmoji()).orElseThrow()));
        slayerModels.forEach(slayerModel -> emojis.put(slayerModel.getKey(), Emoji.of(slayerModel.getEmoji()).orElseThrow()));
        emojis.put(catacombs.getKey(), Emoji.of(catacombs.getEmoji()).orElseThrow());
        dungeonClassModels.forEach(dungeonClassModel -> emojis.put(dungeonClassModel.getKey(), Emoji.of(dungeonClassModel.getEmoji()).orElseThrow()));
        emojis.put("skills", getEmoji("SKILLS").orElseThrow());
        emojis.put("weight", getEmoji("WEIGHT").orElseThrow());
        emojis.put("networth", getEmoji("TRADING_COIN").orElseThrow());
        emojis.put("skyblock", getEmoji("SKYBLOCK").orElseThrow());
        emojis.put("slayer", getEmoji("SLAYER").orElseThrow());

        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");

        Color tagColor = guild.getTagColor().orElse(MinecraftChatFormatting.YELLOW).getColor();
        String guildDescription = guild.getDescription().orElse(guild.getName() + " doesn't have a description set.");
        String guildTag = guild.getTag().orElse("Tag was not found.");
        int guildLevel = guild.getLevel();
        String guildOwner = ignMap.get(guild.getGuildMaster().getUniqueId());
        String pageIdentifier = commandContext.getArgument("page").getValue().orElse("general_information");

        Response response = Response.builder()
            .withReference(commandContext)
            .isInteractable()
            .withTimeToLive(120)
            .withPages(
                Page.builder()
                    .withOption(getOptionBuilder("general_information")
                        .withDescription("Guild Averages and Totals") //TODO: add minion slots to main page?
                        .withEmoji(emojis.get("skyblock"))
                        .build())
                    .withEmbeds(
                        Embed.builder()
                            .withTitle(guild.getName())
                            .withDescription(
                                """
                                {0}
                                
                                Average Weight: **{1}** (Without Overflow: **{2}**)
                                Average Networth: **{3}**
                                Average Skill Level: **{4}**
                                Average Skyblock Level: **{5}**
                                """,
                                guildDescription,
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getTotal()
                                ).average().orElse(0),
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getValue()
                                ).average().orElse(0),
                                (long) guildMemberPlayers.stream().mapToLong(
                                    networths::get
                                ).average().orElse(0),
                                df.format(guildMemberPlayers.stream()
                                    .mapToDouble(SkyBlockIsland.Member::getSkillAverage).average().orElseThrow()),
                                guildMemberPlayers.stream()
                                    .mapToDouble(member -> member.getLeveling().getLevel())
                                    .filter(level -> level > 0)
                                    .average()
                                    .orElse(0)
                            )
                            .withField("Tag", guildTag, true)
                            .withField("Guild Level", String.valueOf(guildLevel), true)
                            .withField("Guild Owner", guildOwner, true)
                            .withColor(tagColor)
                            .build()
                    )
                    .withPages(
                        Page.builder()
                            .withItemData(
                                Page.ItemData.builder(SkyBlockIsland.Member.class)
                                    .withColumnNames(Triple.of("Skyblock Level Leaderboard", "", ""))
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        Page.ItemData.Sorter.<SkyBlockIsland.Member>builder()
                                            .withFunctions(member -> member.getLeveling().getExperience())
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer(stream ->
                                        StreamUtil.mapWithIndex(
                                            stream, (guildMemberPlayer, index, size) -> FieldItem.builder()
                                                .withOptionValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                                .withData(FormatUtil.format(
                                                    " #{0} `{1}` >  **{2}**",
                                                    index + 1,
                                                    ignMap.get(guildMemberPlayer.getUniqueId()),
                                                    guildMemberPlayer.getLeveling().getLevel()
                                                ))
                                                .build()
                                        )
                                    )
                                    .withStyle(PageItem.Style.LIST_SINGLE)
                                    .withAmountPerPage(10)
                                    .build()
                            )
                            .withOption(
                                getOptionBuilder("skyblock_level")
                                    .withDescription("Skyblock Level Leaderboard")
                                    //.withEmoji()
                                    .build()
                            )
                            .withEmbeds(Embed.builder()
                                .withColor(tagColor)
                                .withTitle(guild.getName())
                                .withDescription(
                                    """
                                    Skyblock Level Average: **{0}**
                                    """,
                                    guildMemberPlayers.stream()
                                        .mapToDouble(member -> member.getLeveling().getLevel())
                                        .average()
                                        .orElse(0)
                                )
                                .build()
                            )
                            .build()
                    )
                    .build()
            )
            .withPages(
                Page.builder()
                    .withItemData(
                        Page.ItemData.builder(SkillModel.class)
                            .withPageItems(
                                FieldItem.builder()
                                    .withData(
                                        """
                                            {0}Average Level: **{2}**
                                            {0}Total Experience:
                                            {1}**{3}**
                                            """,
                                        emojiReplyStem,
                                        emojiReplyEnd,
                                        df.format(guildMemberPlayers.stream()
                                            .mapToDouble(SkyBlockIsland.Member::getSkillAverage).average().orElseThrow()),
                                        guildMemberPlayers.stream()
                                            .mapToLong(guildMemberPlayer -> skills.get(guildMemberPlayer).stream().mapToLong(skill -> (long) skill.getExperience()).sum()).sum()
                                    )
                                    .withOption(
                                        getOptionBuilder("all_skills")
                                            .withEmoji(emojis.get("skills"))
                                            .build()
                                    )
                                    .build()
                            )
                            .withItems(skillModels)
                            .withTransformer(stream -> stream
                                .map(skillModel -> FieldItem.builder()
                                    .withEmoji(emojis.get(skillModel.getKey()))
                                    .withData(
                                        FormatUtil.format(
                                            """
                                                {0}Average Level: **{2}**
                                                {0}Total Experience:
                                                {1}**{3}**
                                                """,
                                            emojiReplyStem,
                                            emojiReplyEnd,
                                            df.format(guildMemberPlayers.stream()
                                                .mapToDouble(member -> skills.get(member).stream().filter(skill -> skill.getType().equals(skillModel))
                                                    .findFirst().orElseThrow().getLevel()).average().orElseThrow()),
                                            (long) guildMemberPlayers.stream()
                                                .mapToDouble(member -> skills.get(member).stream().filter(skill -> skill.getType().equals(skillModel))
                                                    .findFirst().orElseThrow().getExperience()).sum()
                                        )
                                    )
                                    .withOption(getOptionBuilder(skillModel.getKey().toLowerCase())
                                        .withEmoji(emojis.get(skillModel.getKey()))
                                        .build()
                                    )
                                    .build()
                                )
                            )
                            .withPageItems(FieldItem.builder().build())
                            .withStyle(PageItem.Style.FIELD_INLINE)
                            .withAmountPerPage(20)
                            .build()
                    )
                    .withOption(
                        getOptionBuilder("skill_averages")
                            .withDescription("Guild Skill Averages and Totals")
                            .withEmoji(emojis.get("skills"))
                            .build())
                    .withEmbeds(
                        Embed.builder()
                            .withTitle(guild.getName())
                            .withDescription(
                                """
                                Average Weight: **{0}** (Without Overflow: **{1}**)
                                Average Networth: **{2}**
                                """,
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getTotal()
                                ).average().orElseThrow(),
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getValue()
                                ).average().orElseThrow(),
                                (long) guildMemberPlayers.stream().mapToLong(
                                    networths::get
                                ).average().orElseThrow()
                            )
                            .withColor(tagColor)
                            .build()
                    )
                    .withPages(skillModels.stream()
                        .map(skillModel -> Page.builder()
                            .withItemData(
                                Page.ItemData.builder(SkyBlockIsland.Member.class)
                                    .withColumnNames(Triple.of(skillModel.getName() + " Leaderboard", "", ""))
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        Page.ItemData.Sorter.<SkyBlockIsland.Member>builder()
                                            .withFunctions(guildMemberPlayer -> skills.get(guildMemberPlayer).stream()
                                                .filter(skill -> skill.getType().equals(skillModel))
                                                .findFirst().orElseThrow().getExperience()
                                            )
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer(stream -> StreamUtil.mapWithIndex(stream, (member, index, size) -> FieldItem.builder()
                                        .withOptionValue(ignMap.get(member.getUniqueId()))
                                        .withData(FormatUtil.format(
                                            " #{0} `{1}` >  **{2} [{3}]**",
                                            index + 1,
                                            ignMap.get(member.getUniqueId()),
                                            (long) skills.get(member).stream().filter(skill -> skill.getType().equals(skillModel)).findFirst().orElseThrow().getExperience(),
                                            skills.get(member).stream().filter(skill -> skill.getType().equals(skillModel)).findFirst().orElseThrow().getLevel()
                                        ))
                                        .build()
                                    ))
                                    .withStyle(PageItem.Style.LIST_SINGLE)
                                    .withAmountPerPage(20)
                                    .build()
                            )
                            .withOption(
                                getOptionBuilder(skillModel.getKey().toLowerCase() + "_leaderboard")
                                    .withDescription("Guild Leaderboard for the " + skillModel.getName() + " Skill")
                                    .withEmoji(emojis.get(skillModel.getKey()))
                                    .build()
                            )
                            .withEmbeds(
                                Embed.builder()
                                    .withColor(tagColor)
                                    .withTitle(guild.getName())
                                    .withDescription(FormatUtil.format(
                                        """
                                        {0} Average: **{1}** / {2}
                                        """,
                                        skillModel.getName(),
                                        df.format(guildMemberPlayers.stream()
                                            .mapToDouble(guildMemberPlayer -> skills.get(guildMemberPlayer).stream().filter(skill -> skill.getType().equals(skillModel))
                                                .findFirst().orElseThrow().getLevel()).average().orElseThrow()),
                                        skillModel.getMaxLevel()
                                        )
                                    )
                                    .build()
                            )
                            .build()
                        )
                        .collect(Concurrent.toList())
                    )
                    .build()
            )
            .withPages(
                Page.builder()
                    .withItemData(
                        Page.ItemData.builder(SlayerModel.class)
                            .withItems(slayerModels)
                            .withTransformer(stream -> stream
                                .map(slayerModel -> FieldItem.builder()
                                    .withEmoji(emojis.get(slayerModel.getKey()))
                                    .withOptionValue(slayerModel.getKey())
                                    .withData(
                                        FormatUtil.format(
                                            """
                                                {0}Average Level: **{2}**
                                                {0}Total Experience:
                                                {1}**{3}**
                                                """,
                                            emojiReplyStem,
                                            emojiReplyEnd,
                                            df.format(guildMemberPlayers.stream()
                                                .mapToDouble(member -> member.getSlayer(slayerModel).getLevel()).average().orElseThrow()),
                                            (long) guildMemberPlayers.stream()
                                                .mapToDouble(member -> member.getSlayer(slayerModel).getExperience()).sum()
                                        )
                                    )
                                    .withOption(
                                        getOptionBuilder(slayerModel.getName().replace(" ", "_").toLowerCase())
                                            .withEmoji(emojis.get(slayerModel.getKey()))
                                            .build()
                                    )
                                    .build()
                                )
                            )
                            .withPageItems(FieldItem.builder().build())
                            .withStyle(PageItem.Style.FIELD_INLINE)
                            .withAmountPerPage(20)
                            .build()
                    )
                    .withOption(
                        getOptionBuilder("slayer_information")
                            .withDescription("Guild Slayer Averages and Totals")
                            .withEmoji(emojis.get("slayer"))
                            .build()
                    )
                    .withEmbeds(
                        Embed.builder()
                            .withTitle(guild.getName())
                            .withDescription(
                                """
                                Average Weight: **{0}** (Without Overflow: **{1}**)
                                Average Networth: **{2}**
                                """,
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getTotal()
                                ).average().orElseThrow(),
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getValue()
                                ).average().orElseThrow(),
                                (long) guildMemberPlayers.stream().mapToLong(
                                    networths::get
                                ).average().orElseThrow()
                            )
                            .withColor(tagColor)
                            .build()
                    )
                    .withPages(slayerModels.stream()
                        .map(slayerModel ->
                            Page.builder()
                                .withItemData(
                                    Page.ItemData.builder(SkyBlockIsland.Member.class)
                                        .withColumnNames(Triple.of(slayerModel.getName() + " Leaderboard", "", ""))
                                        .withItems(guildMemberPlayers)
                                        .withSorters(
                                            Page.ItemData.Sorter.<SkyBlockIsland.Member>builder()
                                                .withFunctions(guildMemberPlayer -> guildMemberPlayer.getSlayer(slayerModel).getExperience())
                                                .withOrder(SortOrder.DESCENDING)
                                                .build()
                                        )
                                        .withTransformer(stream ->
                                            StreamUtil.mapWithIndex(
                                                stream,
                                                (guildMemberPlayer, index, size) -> FieldItem.builder()
                                                    .withOptionValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                                    .withData(FormatUtil.format(
                                                        " #{0} `{1}` >  **{2} [{3}]**",
                                                        index + 1,
                                                        ignMap.get(guildMemberPlayer.getUniqueId()),
                                                        (long) guildMemberPlayer.getSlayer(slayerModel).getExperience(),
                                                        guildMemberPlayer.getSlayer(slayerModel).getLevel()
                                                    ))
                                                    .build()
                                            )
                                        )
                                        .withStyle(PageItem.Style.LIST_SINGLE)
                                        .withAmountPerPage(20)
                                        .build()
                                )
                                .withOption(
                                    getOptionBuilder(slayerModel.getName().replace(" ", "_").toLowerCase() + "_leaderboard")
                                        .withDescription("Guild Leaderboard for the " + slayerModel.getName() + " Slayer")
                                        .withEmoji(emojis.get(slayerModel.getKey()))
                                        .build())
                                .withEmbeds(
                                    Embed.builder()
                                        .withColor(tagColor)
                                        .withTitle(guild.getName())
                                        .withDescription(
                                            """
                                            {0} Average: **{1}** / {2}
                                            """,
                                            slayerModel.getName(),
                                            df.format(guildMemberPlayers.stream()
                                                .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getSlayer(slayerModel).getLevel()).average().orElseThrow()),
                                            9
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .collect(Concurrent.toList())
                    )
                    .build()
            )
            .withPages(
                Page.builder()
                    .withItemData(
                        Page.ItemData.builder(DungeonClassModel.class)
                            .withPageItems(FieldItem.builder()
                                .withEmoji(emojis.get(catacombs.getKey()))
                                .withLabel(
                                    FormatUtil.format(
                                        """
                                            {0}Average Level: **{2}**
                                            {0}Total Experience:
                                            {1}**{3}**
                                            """,
                                        emojiReplyStem,
                                        emojiReplyEnd,
                                        df.format(guildMemberPlayers.stream()
                                            .mapToDouble(member -> member.getDungeons().getDungeon(catacombs).getLevel()).average().orElseThrow()),
                                        (long) guildMemberPlayers.stream()
                                            .mapToDouble(member -> member.getDungeons().getDungeon(catacombs).getExperience()).sum()
                                    )
                                )
                                .withOption(
                                    getOptionBuilder(catacombs.getKey().toLowerCase())
                                        .withEmoji(emojis.get(catacombs.getKey()))
                                        .build()
                                )
                                .build()
                            )
                            .withItems(dungeonClassModels)
                            .withTransformer(stream -> stream
                                .map(dungeonClassModel -> FieldItem.builder()
                                    .withEmoji(emojis.get(dungeonClassModel.getKey()))
                                    .withData(
                                        FormatUtil.format(
                                            """
                                                {0}Average Level: **{2}**
                                                {0}Total Experience:
                                                {1}**{3}**
                                                """,
                                            emojiReplyStem,
                                            emojiReplyEnd,
                                            df.format(guildMemberPlayers.stream()
                                                .mapToDouble(member -> member.getDungeons().getClass(dungeonClassModel).getLevel()).average().orElseThrow()),
                                            (long) guildMemberPlayers.stream()
                                                .mapToDouble(member -> member.getDungeons().getClass(dungeonClassModel).getExperience()).sum()
                                        )
                                    )
                                    .withOption(
                                        getOptionBuilder(dungeonClassModel.getKey().toLowerCase())
                                            .withEmoji(emojis.get(dungeonClassModel.getKey()))
                                            .build()
                                    )
                                    .build()
                                )
                            )
                            .withStyle(PageItem.Style.FIELD_INLINE)
                            .withAmountPerPage(20)
                            .build()
                    )
                    .withOption(
                        getOptionBuilder("dungeon_information")
                            .withDescription("Guild Dungeon Averages and Totals")
                            .withEmoji(emojis.get(catacombs.getKey()))
                            .build())
                    .withEmbeds(
                        Embed.builder()
                            .withTitle(guild.getName())
                            .withDescription(
                                """
                                Average Weight: **{0}** (Without Overflow: **{1}**)
                                Average Networth: **{2}**
                                """,
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getTotal()
                                ).average().orElseThrow(),
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getValue()
                                ).average().orElseThrow(),
                                (long) guildMemberPlayers.stream().mapToLong(
                                    networths::get
                                ).average().orElseThrow()
                            )
                            .withColor(tagColor)
                            .build()
                    )
                    .withPages(
                        Page.builder()
                            .withItemData(
                                Page.ItemData.builder(SkyBlockIsland.Member.class)
                                    .withColumnNames(Triple.of("Catacombs Leaderboard", "", ""))
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        Page.ItemData.Sorter.<SkyBlockIsland.Member>builder()
                                            .withFunctions(guildMemberPlayer -> guildMemberPlayer.getDungeons().getDungeon(catacombs).getExperience())
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer(stream ->
                                        StreamUtil.mapWithIndex(
                                            stream,
                                            (guildMemberPlayer, index, size) -> FieldItem.builder()
                                                .withOptionValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                                .withData(
                                                    FormatUtil.format(
                                                        " #{0} `{1}` >  **{2} [{3}]**",
                                                        index + 1,
                                                        ignMap.get(guildMemberPlayer.getUniqueId()),
                                                        (long) guildMemberPlayer.getDungeons().getDungeon(catacombs).getExperience(),
                                                        guildMemberPlayer.getDungeons().getDungeon(catacombs).getLevel()
                                                    )
                                                )
                                                .build()
                                        )
                                    )
                                    .withStyle(PageItem.Style.LIST_SINGLE)
                                    .withAmountPerPage(20)
                                    .build()
                            )
                            .withOption(
                                getOptionBuilder("catacombs_leaderboard")
                                    .withDescription("Guild Leaderboard for Catacombs Level")
                                    .withEmoji(emojis.get(catacombs.getKey()))
                                    .build())
                            .withEmbeds(
                                Embed.builder()
                                    .withColor(tagColor)
                                    .withTitle(guild.getName())
                                    .withDescription(
                                        """
                                        Catacombs Average: **{0}** / 50
                                        """,
                                        df.format(guildMemberPlayers.stream()
                                            .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getDungeons().getDungeon(catacombs).getLevel()).average().orElseThrow())
                                    )
                                    .build()
                            )

                            .build()
                    )
                    .withPages(dungeonClassModels.stream()
                        .map(classModel -> Page.builder()
                            .withItemData(
                                Page.ItemData.builder(SkyBlockIsland.Member.class)
                                    .withColumnNames(Triple.of(classModel.getName() + " Leaderboard", "", ""))
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        Page.ItemData.Sorter.<SkyBlockIsland.Member>builder()
                                            .withFunctions(guildMemberPlayer -> guildMemberPlayer.getDungeons().getClass(classModel).getExperience())
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer(stream ->
                                        StreamUtil.mapWithIndex(
                                            stream,
                                            (guildMemberPlayer, index, size) -> FieldItem.builder()
                                                .withOptionValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                                .withData(
                                                    FormatUtil.format(
                                                        " #{0} `{1}` >  **{2} [{3}]**",
                                                        index + 1,
                                                        ignMap.get(guildMemberPlayer.getUniqueId()),
                                                        (long) guildMemberPlayer.getDungeons().getClass(classModel).getExperience(),
                                                        guildMemberPlayer.getDungeons().getClass(classModel).getLevel()
                                                    )
                                                )
                                                .build()
                                        )
                                    )
                                    .withStyle(PageItem.Style.LIST_SINGLE)
                                    .withAmountPerPage(20)
                                    .build()
                            )
                            .withOption(
                                getOptionBuilder(classModel.getKey().toLowerCase() + "_leaderboard")
                                    .withDescription("Guild Leaderboard for the " + classModel.getName() + " Class")
                                    .withEmoji(emojis.get(classModel.getKey()))
                                    .build())
                            .withEmbeds(
                                Embed.builder()
                                    .withColor(tagColor)
                                    .withTitle(guild.getName())
                                    .withDescription(
                                        """
                                        {0} Average: **{1}** / {2}
                                        """,
                                        classModel.getName(),
                                        df.format(guildMemberPlayers.stream()
                                            .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getDungeons().getClass(classModel).getLevel()).average().orElseThrow()),
                                        50
                                    )
                                    .build()
                            )
                            .build()
                        )
                        .collect(Concurrent.toList())
                    )
                    .build()
            )
            .withPages(
                Page.builder()
                    .withItemData(
                        Page.ItemData.builder(SkyBlockIsland.Member.class)
                            .withColumnNames(Triple.of("Weight Leaderboard", "", ""))
                            .withItems(guildMemberPlayers)
                            .withSorters(
                                Page.ItemData.Sorter.<SkyBlockIsland.Member>builder()
                                    .withFunctions(guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getTotal())
                                    .withOrder(SortOrder.DESCENDING)
                                    .build()
                            )
                            .withTransformer(stream ->
                                StreamUtil.mapWithIndex(
                                    stream,
                                    (guildMemberPlayer, index, size) -> FieldItem.builder()
                                        .withOptionValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                        .withData(FormatUtil.format(
                                            " #{0} `{1}` >  **{2} [{3}]**",
                                            index + 1,
                                            ignMap.get(guildMemberPlayer.getUniqueId()),
                                            (int) totalWeights.get(guildMemberPlayer).getTotal(),
                                            (int) totalWeights.get(guildMemberPlayer).getValue()
                                        ))
                                        .build()
                                )
                            )
                            .withStyle(PageItem.Style.LIST_SINGLE)
                            .withAmountPerPage(20)
                            .build()
                    )
                    .withOption(
                        getOptionBuilder("weight_leaderboard")
                            .withDescription("Guild Weight Leaderboard")
                            .withEmoji(emojis.get("weight"))
                            .build())
                    .withEmbeds(
                        Embed.builder()
                            .withColor(tagColor)
                            .withTitle(guild.getName())
                            .withDescription(
                                """
                                Average Weight: **{0}** (Without Overflow: **{1}**)
                                """,
                                (int) (guildMemberPlayers.stream()
                                    .mapToDouble(guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getTotal()).average().orElseThrow()),
                                (int) (guildMemberPlayers.stream()
                                    .mapToDouble(guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getValue()).average().orElseThrow())
                            )
                            .build()
                    )
                    .build()
            )
            .withPages(
                Page.builder()
                    .withItemData(
                        Page.ItemData.builder(SkyBlockIsland.Member.class)
                            .withColumnNames(Triple.of("Networth Leaderboard", "", ""))
                            .withItems(guildMemberPlayers)
                            .withSorters(
                                Page.ItemData.Sorter.<SkyBlockIsland.Member>builder()
                                    .withFunctions(networths::get)
                                    .withOrder(SortOrder.DESCENDING)
                                    .build()
                            )
                            .withTransformer(stream ->
                                StreamUtil.mapWithIndex(
                                    stream,
                                    (guildMemberPlayer, index, size) -> FieldItem.builder()
                                        .withOptionValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                        .withData(FormatUtil.format(
                                            " #{0} `{1}` >  **{2}**",
                                            index + 1,
                                            ignMap.get(guildMemberPlayer.getUniqueId()),
                                            (int) guildMemberPlayer.getPurse()
                                        ))
                                        .build()
                                )
                            )
                            .withStyle(PageItem.Style.LIST_SINGLE)
                            .withAmountPerPage(20)
                            .build()
                    )
                    .withOption(
                        getOptionBuilder("networth_leaderboard")
                            .withDescription("Guild Networth Leaderboard")
                            .withEmoji(emojis.get("networth"))
                            .build())
                    .withEmbeds(
                        Embed.builder()
                            .withColor(tagColor)
                            .withTitle(guild.getName())
                            .withDescription(FormatUtil.format("""
                                Average Networth: **{0}**
                                Total Networth: **{1}**
                            """,
                                (long) (guildMemberPlayers.stream()
                                    .mapToDouble(networths::get).average().orElseThrow()),
                                (long) (guildMemberPlayers.stream()
                                    .mapToDouble(networths::get).sum()))
                            )
                            .build()
                    )
                    .build()
            )
            .withDefaultPage(pageIdentifier)
            .build();
        return commandContext.reply(response);
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder("name", "Name of the Guild to look up", Parameter.Type.TEXT)
                .isRequired()
                .build(),
            Parameter.builder("page", "Jump to a specific page", Parameter.Type.TEXT)
                .withChoices(
                    pageIdentifiers.stream()
                        .map(pageIdentifier -> Pair.of(WordUtil.capitalizeFully(pageIdentifier.replace("_", " ")), pageIdentifier))
                        .collect(Concurrent.toLinkedMap())
                )
                .build()
        );
    }

    private static SelectMenu.Option.Builder getOptionBuilder(String identifier) {
        return SelectMenu.Option.builder()
            .withValue(identifier)
            .withLabel(WordUtil.capitalizeFully(identifier.replace("_", " ")));
    }
}
