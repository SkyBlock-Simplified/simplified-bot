package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.implementation.HypixelPlayerData;
import dev.sbs.api.client.hypixel.implementation.HypixelSkyBlockData;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelPlayerResponse;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
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
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.StreamUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.Argument;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.PageItem;
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

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        String guildName = commandContext.getArgument("name").flatMap(Argument::getValue).orElseThrow();

        HypixelGuildResponse hypixelGuildResponse = SimplifiedApi.getWebApi(HypixelPlayerData.class).getGuildByName(guildName);

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
        HypixelSkyBlockData skyBlockData = SimplifiedApi.getWebApi(HypixelSkyBlockData.class);
        HypixelGuildResponse.Guild guild = hypixelGuildResponse.getGuild().get();
        HypixelPlayerData hypixelPlayerData = SimplifiedApi.getWebApi(HypixelPlayerData.class);

        ConcurrentMap<HypixelPlayerResponse.Player, SkyBlockIsland> guildMembers = guild.getMembers().stream()
            .limit(2) //TODO: limiting size!!
            .map(member -> Pair.of(
                hypixelPlayerData.getPlayer(member.getUniqueId()).getPlayer(),
                skyBlockData.getProfiles(member.getUniqueId()).getLastPlayed())
            )
            .collect(Concurrent.toMap());

        ConcurrentMap<UUID, String> ignMap = guildMembers.keySet().stream()
            .map(key -> Pair.of(key.getUniqueId(), key.getDisplayName()))
            .collect(Concurrent.toMap());

        ConcurrentList<SkyBlockIsland.Member> guildMemberPlayers = guildMembers.keySet().stream()
            .map(hypixelPlayer -> guildMembers.get(hypixelPlayer).getMember(hypixelPlayer.getUniqueId()).orElseThrow())
            .collect(Concurrent.toList());

        ConcurrentMap<SkyBlockIsland.Member, SkyBlockIsland.Experience.Weight> totalWeights = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getTotalWeight()))
            .collect(Concurrent.toMap());

        ConcurrentMap<SkyBlockIsland.Member, Long> networths = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getPurse())) //TODO: networth query
            .collect(Concurrent.toMap());

        ConcurrentMap<SkyBlockIsland.Member, ConcurrentList<SkyBlockIsland.Skill>> skills = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getSkills()))
            .collect(Concurrent.toMap());

//        ConcurrentMap<SkyBlockIsland.Member, ConcurrentList<SkyBlockIsland.Slayer>> slayers = guildMemberPlayers.stream()
//            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getSlayers())) //TODO: clear up whether slayers should be cached or not; getSlayers calls getRepository, getSlayer pulls from list -> same thing with dungeon classes
//            .collect(Concurrent.toMap());

        ConcurrentMap<String, Emoji> emojis = new ConcurrentMap<>();
        skillModels.forEach(skillModel -> emojis.put(skillModel.getKey(), Emoji.of(skillModel.getEmoji()).orElseThrow()));
        slayerModels.forEach(slayerModel -> emojis.put(slayerModel.getKey(), Emoji.of(slayerModel.getEmoji()).orElseThrow()));
        emojis.put(catacombs.getKey(), Emoji.of(catacombs.getEmoji()).orElseThrow());
        dungeonClassModels.forEach(dungeonClassModel -> emojis.put(dungeonClassModel.getKey(), Emoji.of(dungeonClassModel.getEmoji()).orElseThrow()));
        emojis.put("skills", getEmoji("SKILLS").orElseThrow());
        emojis.put("weight", getEmoji("MUSCLE").orElseThrow());
        emojis.put("networth", getEmoji("TRADING_COIN").orElseThrow());
        emojis.put("skyblock", getEmoji("SKYBLOCK").orElseThrow());
        emojis.put("slayer", getEmoji("SLAYER").orElseThrow());

        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");

        Color tagColor = guild.getTagColor().orElse(MinecraftChatFormatting.YELLOW).getColor();
        String guildDescription = guild.getDescription().orElse(guildName + " doesn't have a description set.");
        String guildTag = guild.getTag().orElse("Tag was not found.");

        Response response = Response.builder()
            .withReference(commandContext)
            .isInteractable()
            .withTimeToLive(120)
            .withPages(Page.builder()
                .withPageItemStyle(PageItem.Style.FIELD_INLINE)
                .withItemsPerPage(12)
                .withOption(SelectMenu.Option.builder()
                    .withLabel("General Information") //TODO: add minion slots to main page?
                    .withDescription("Guild Averages and Totals")
                    .withValue("General Information")
                    .withEmoji(emojis.get("skyblock"))
                    .build())
                .withEmbeds(Embed.builder()
                    .withTitle(guildName)
                    .withDescription(FormatUtil.format("""
                        {0}
                        Tag: {1}
                        Average Weight: **{2}** (Without Overflow: **{3}**)
                        Average Networth: **{4}**
                        """,
                        guildDescription,
                        guildTag,
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
                    )
                    .withColor(tagColor)
                    .build()
                )
                .withItems(
                    PageItem.builder()
                        .withData(FormatUtil.format(
                            """
                                {0}Average Level: **{2}**
                                {0}Total Experience:
                                {1}**{3}**
                                """,
                            emojiReplyStem,
                            emojiReplyEnd,
                            df.format(guildMemberPlayers.stream()
                                .mapToDouble(guildMemberPlayer -> getSkillAverage(skills.get(guildMemberPlayer))).average().orElseThrow()),
                            guildMemberPlayers.stream()
                                .mapToLong(guildMemberPlayer -> skills.get(guildMemberPlayer).stream().mapToLong(skill -> (long) skill.getExperience()).sum()).sum()
                        ))
                        .withOption(
                            SelectMenu.Option.builder()
                                .withLabel("All Skills")
                                .withValue("All Skills")
                                .withEmoji(emojis.get("skills"))
                                .build()
                        )
                        .build()
                )
                .build()
            )



            .withPages(Page.builder()
                .withPageItemStyle(PageItem.Style.FIELD_INLINE)
                .withItemsPerPage(20)
                .withOption(SelectMenu.Option.builder()
                    .withLabel("Skill Averages")
                    .withDescription("Guild Skill Averages and Totals")
                    .withValue("Skill Averages")
                    .withEmoji(emojis.get("skills"))
                    .build())
                .withEmbeds(Embed.builder()
                    .withTitle(guildName)
                    .withDescription(FormatUtil.format("""
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
                    )
                    .withColor(tagColor)
                    .build()
                )
                .withItems(
                    PageItem.builder()
                        .withData(FormatUtil.format(
                            """
                                {0}Average Level: **{2}**
                                {0}Total Experience:
                                {1}**{3}**
                                """,
                            emojiReplyStem,
                            emojiReplyEnd,
                            df.format(guildMemberPlayers.stream()
                                .mapToDouble(guildMemberPlayer -> getSkillAverage(skills.get(guildMemberPlayer))).average().orElseThrow()),
                            guildMemberPlayers.stream()
                                .mapToLong(guildMemberPlayer -> skills.get(guildMemberPlayer).stream().mapToLong(skill -> (long) skill.getExperience()).sum()).sum()
                        ))
                        .withOption(
                            SelectMenu.Option.builder()
                                .withLabel("All Skills")
                                .withValue("All Skills")
                                .withEmoji(emojis.get("skills"))
                                .build()
                        )
                        .build()

                )
                .withItems(skillModels
                    .stream()
                    .map(skillModel -> PageItem.builder()
                        .withEmoji(emojis.get(skillModel.getKey()))
                        .withData(FormatUtil.format(
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
                        .withOption(SelectMenu.Option.builder()
                            .withLabel(skillModel.getName())
                            .withValue(skillModel.getName())
                            .withEmoji(emojis.get(skillModel.getKey()))
                            .build()
                        )
                        .build()
                    )
                    .collect(Concurrent.toList())
                )
                .withPages(skillModels.stream()
                    .map(skillModel -> Page.builder()
                        .withPageItemStyle(PageItem.Style.SINGLE_COLUMN)
                        .withItemsPerPage(20)
                        .withOption(SelectMenu.Option.builder()
                            .withLabel(skillModel.getName() + " Leaderboard")
                            .withDescription("Guild Leaderboard for the " + skillModel.getName() + " Skill")
                            .withValue(skillModel.getName() + " Leaderboard")
                            .withEmoji(emojis.get(skillModel.getKey()))
                            .build())
                        .withEmbeds(
                            Embed.builder()
                                .withColor(tagColor)
                                .withTitle(guildName)
                                .withDescription(FormatUtil.format("""
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
                        .withItems(
                            StreamUtil.mapWithIndex(
                                    guildMemberPlayers.sorted(SortOrder.DESCENDING, guildMemberPlayer -> skills.get(guildMemberPlayer).stream()
                                        .filter(skill -> skill.getType().equals(skillModel))
                                        .findFirst().orElseThrow().getExperience()).stream(),
                                    (guildMemberPlayer, index, size) -> PageItem.builder()
                                        .withValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                        .withLabel(FormatUtil.format(
                                            " #{0} `{1}` >  **{2} [{3}]**",
                                            index + 1,
                                            ignMap.get(guildMemberPlayer.getUniqueId()),
                                            (long) skills.get(guildMemberPlayer).stream().filter(skill -> skill.getType().equals(skillModel)).findFirst().orElseThrow().getExperience(),
                                            skills.get(guildMemberPlayer).stream().filter(skill -> skill.getType().equals(skillModel)).findFirst().orElseThrow().getLevel()
                                        ))
                                        .build()
                                )
                                .collect(Concurrent.toList())
                        )
                        .build()
                    )
                    .collect(Concurrent.toList())
                )
                .build()
            )



            .withPages(Page.builder()
                .withPageItemStyle(PageItem.Style.FIELD_INLINE)
                .withItemsPerPage(20)
                .withOption(SelectMenu.Option.builder()
                    .withLabel("Slayer Information")
                    .withDescription("Guild Slayer Averages and Totals")
                    .withValue("Slayer Information")
                    .withEmoji(emojis.get("slayer"))
                    .build()
                )
                .withEmbeds(
                    Embed.builder()
                        .withTitle(guildName)
                        .withDescription(FormatUtil.format("""
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
                        )
                        .withColor(tagColor)
                        .build()
                )
                .withItems(slayerModels.stream()
                    .map(slayerModel -> PageItem.builder()
                        .withEmoji(emojis.get(slayerModel.getKey()))
                        .withValue(slayerModel.getKey())
                        .withLabel(FormatUtil.format(
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
                        .withOption(SelectMenu.Option.builder()
                            .withLabel(slayerModel.getName())
                            .withEmoji(emojis.get(slayerModel.getKey()))
                            .withValue(slayerModel.getName())
                            .build()
                        )
                        .build()
                    )
                    .collect(Concurrent.toList())
                )
                .withPages(slayerModels.stream()
                    .map(slayerModel -> Page.builder()
                        .withPageItemStyle(PageItem.Style.SINGLE_COLUMN)
                        .withItemsPerPage(20)
                        .withOption(SelectMenu.Option.builder()
                            .withLabel(slayerModel.getName() + " Leaderboard")
                            .withDescription("Guild Leaderboard for the " + slayerModel.getName() + " Slayer")
                            .withValue(slayerModel.getName() + " Leaderboard")
                            .withEmoji(emojis.get(slayerModel.getKey()))
                            .build())
                        .withEmbeds(
                            Embed.builder()
                                .withColor(tagColor)
                                .withTitle(guildName)
                                .withDescription(FormatUtil.format("""
                                {0} Average: **{1}** / {2}
                                """,
                                        slayerModel.getName(),
                                        df.format(guildMemberPlayers.stream()
                                            .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getSlayer(slayerModel).getLevel()).average().orElseThrow()),
                                        9
                                    )
                                )
                                .build()
                        )
                        .withItems(
                            StreamUtil.mapWithIndex(
                                    guildMemberPlayers.sorted(SortOrder.DESCENDING, guildMemberPlayer -> guildMemberPlayer.getSlayer(slayerModel).getExperience()).stream(),
                                    (guildMemberPlayer, index, size) -> PageItem.builder()
                                        .withValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                        .withLabel(FormatUtil.format(
                                            " #{0} `{1}` >  **{2} [{3}]**",
                                            index + 1,
                                            ignMap.get(guildMemberPlayer.getUniqueId()),
                                            (long) guildMemberPlayer.getSlayer(slayerModel).getExperience(),
                                            guildMemberPlayer.getSlayer(slayerModel).getLevel()
                                        ))
                                        .build()
                                )
                                .collect(Concurrent.toList())
                        )
                        .build()
                    )
                    .collect(Concurrent.toList())
                )
                .build()
            )



            .withPages(Page.builder()
                .withPageItemStyle(PageItem.Style.FIELD_INLINE)
                .withItemsPerPage(20)
                .withOption(SelectMenu.Option.builder()
                    .withLabel("Dungeon Information")
                    .withDescription("Guild Dungeon Averages and Totals")
                    .withValue("Dungeon Information")
                    .withEmoji(emojis.get(catacombs.getKey()))
                    .build())
                .withEmbeds(Embed.builder()
                    .withTitle(guildName)
                    .withDescription(FormatUtil.format("""
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
                    )
                    .withColor(tagColor)
                    .build()
                )
                .withItems(PageItem.builder()
                    .withEmoji(emojis.get(catacombs.getKey()))
                    .withLabel(FormatUtil.format(
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
                    .withOption(SelectMenu.Option.builder()
                        .withLabel(catacombs.getName())
                        .withEmoji(emojis.get(catacombs.getKey()))
                        .withValue(catacombs.getName())
                        .build()
                    )
                    .build()
                )
                .withItems(dungeonClassModels.stream()
                    .map(dungeonClassModel -> PageItem.builder()
                        .withEmoji(emojis.get(dungeonClassModel.getKey()))
                        .withData(FormatUtil.format(
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
                        .withOption(SelectMenu.Option.builder()
                            .withLabel(dungeonClassModel.getName())
                            .withEmoji(emojis.get(dungeonClassModel.getKey()))
                            .withValue(dungeonClassModel.getName())
                            .build()
                        )
                        .build()
                    )
                    .collect(Concurrent.toList())
                )
                .withPages(Page.builder()
                    .withPageItemStyle(PageItem.Style.SINGLE_COLUMN)
                    .withItemsPerPage(20)
                    .withOption(SelectMenu.Option.builder()
                        .withLabel("Catacombs Leaderboard")
                        .withDescription("Guild Leaderboard for Catacombs Level")
                        .withValue("Catacombs Leaderboard")
                        .withEmoji(emojis.get(catacombs.getKey()))
                        .build())
                    .withEmbeds(
                        Embed.builder()
                            .withColor(tagColor)
                            .withTitle(guildName)
                            .withDescription(FormatUtil.format("""
                            Catacombs Average: **{0}** / 50
                            """,
                                    df.format(guildMemberPlayers.stream()
                                        .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getDungeons().getDungeon(catacombs).getLevel()).average().orElseThrow())
                                )
                            )
                            .build()
                    )
                    .withItems(
                        StreamUtil.mapWithIndex(
                                guildMemberPlayers.sorted(SortOrder.DESCENDING, guildMemberPlayer -> guildMemberPlayer.getDungeons().getDungeon(catacombs).getExperience()).stream(),
                                (guildMemberPlayer, index, size) -> PageItem.builder()
                                    .withValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                    .withLabel(FormatUtil.format(
                                        " #{0} `{1}` >  **{2} [{3}]**",
                                        index + 1,
                                        ignMap.get(guildMemberPlayer.getUniqueId()),
                                        (long) guildMemberPlayer.getDungeons().getDungeon(catacombs).getExperience(),
                                        guildMemberPlayer.getDungeons().getDungeon(catacombs).getLevel()
                                    ))
                                    .build()
                            )
                            .collect(Concurrent.toList())
                    )
                    .build()
                )
                .withPages(dungeonClassModels.stream()
                    .map(classModel -> Page.builder()
                        .withPageItemStyle(PageItem.Style.SINGLE_COLUMN)
                        .withItemsPerPage(20)
                        .withOption(SelectMenu.Option.builder()
                            .withLabel(classModel.getName() + " Leaderboard")
                            .withDescription("Guild Leaderboard for the " + classModel.getName() + " Class")
                            .withValue(classModel.getName() + " Leaderboard")
                            .withEmoji(emojis.get(classModel.getKey()))
                            .build())
                        .withEmbeds(
                            Embed.builder()
                                .withColor(tagColor)
                                .withTitle(guildName)
                                .withDescription(FormatUtil.format("""
                                {0} Average: **{1}** / {2}
                                """,
                                        classModel.getName(),
                                        df.format(guildMemberPlayers.stream()
                                            .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getDungeons().getClass(classModel).getLevel()).average().orElseThrow()),
                                        50
                                    )
                                )
                                .build()
                        )
                        .withItems(
                            StreamUtil.mapWithIndex(
                                    guildMemberPlayers.sorted(SortOrder.DESCENDING, guildMemberPlayer -> guildMemberPlayer.getDungeons().getClass(classModel).getExperience()).stream(),
                                    (guildMemberPlayer, index, size) -> PageItem.builder()
                                        .withValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                                        .withLabel(FormatUtil.format(
                                            " #{0} `{1}` >  **{2} [{3}]**",
                                            index + 1,
                                            ignMap.get(guildMemberPlayer.getUniqueId()),
                                            (long) guildMemberPlayer.getDungeons().getClass(classModel).getExperience(),
                                            guildMemberPlayer.getDungeons().getClass(classModel).getLevel()
                                        ))
                                        .build()
                                )
                                .collect(Concurrent.toList())
                        )
                        .build()
                    )
                    .collect(Concurrent.toList())
                )
                .build()
            )



            .withPages(Page.builder()
                .withPageItemStyle(PageItem.Style.SINGLE_COLUMN)
                .withItemsPerPage(20)
                .withOption(SelectMenu.Option.builder()
                    .withLabel("Weight Leaderboard")
                    .withDescription("Guild Weight Leaderboard")
                    .withValue("Weight Leaderboard")
                    .withEmoji(emojis.get("weight"))
                    .build())
                .withEmbeds(
                    Embed.builder()
                        .withColor(tagColor)
                        .withTitle(guildName)
                        .withDescription(FormatUtil.format("""
                            Average Weight: **{0}** (Without Overflow: **{1}**)
                            """,
                            (int) (guildMemberPlayers.stream()
                                .mapToDouble(guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getTotal()).average().orElseThrow()),
                            (int) (guildMemberPlayers.stream()
                                .mapToDouble(guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getValue()).average().orElseThrow())
                            )
                        )
                        .build()
                )
                .withItems(
                    StreamUtil.mapWithIndex(
                        guildMemberPlayers.sorted(SortOrder.DESCENDING, guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getTotal()).stream(),
                        (guildMemberPlayer, index, size) -> PageItem.builder()
                            .withValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                            .withLabel(FormatUtil.format(
                                " #{0} `{1}` >  **{2} [{3}]**",
                                index + 1,
                                ignMap.get(guildMemberPlayer.getUniqueId()),
                                (int) totalWeights.get(guildMemberPlayer).getTotal(),
                                (int) totalWeights.get(guildMemberPlayer).getValue()
                            ))
                            .build()
                        )
                        .collect(Concurrent.toList())
                )
                .build()
            )



            .withPages(Page.builder()
                .withPageItemStyle(PageItem.Style.SINGLE_COLUMN)
                .withItemsPerPage(20)
                .withOption(SelectMenu.Option.builder()
                    .withLabel("Networth Leaderboard")
                    .withDescription("Guild Networth Leaderboard")
                    .withValue("Networth Leaderboard")
                    .withEmoji(emojis.get("networth"))
                    .build())
                .withEmbeds(
                    Embed.builder()
                        .withColor(tagColor)
                        .withTitle(guildName)
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
                .withItems(
                StreamUtil.mapWithIndex(
                        guildMemberPlayers.sorted(SortOrder.DESCENDING, networths::get).stream(),
                        (guildMemberPlayer, index, size) -> PageItem.builder()
                            .withValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                            .withLabel(FormatUtil.format(
                                " #{0} `{1}` >  **{2}**",
                                index + 1,
                                ignMap.get(guildMemberPlayer.getUniqueId()),
                                (int) guildMemberPlayer.getPurse()
                            ))
                            .build()
                    )
                    .collect(Concurrent.toList())
                )
                .build()
            )
            .build();
        return commandContext.reply(response);
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder("name", "Name of the Guild to look up", Parameter.Type.TEXT)
                .isRequired()
                .build(),
            Parameter.builder("page", "Jump to a specific page", Parameter.Type.TEXT) //TODO: implement choices
                .build()
        );
    }

    private static double getSkillAverage(ConcurrentList<SkyBlockIsland.Skill> skills) {
        return skills.stream()
            .filter(skill -> skill.getType().isCosmetic())
            .mapToInt(SkyBlockIsland.Experience::getLevel).average().orElseThrow();
    }
}
