package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.implementation.HypixelPlayerData;
import dev.sbs.api.client.hypixel.implementation.HypixelSkyBlockData;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.sbs.implementation.MojangData;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.Model;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeon_classes.DungeonClassModel;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeons.DungeonModel;
import dev.sbs.api.data.model.skyblock.skills.SkillModel;
import dev.sbs.api.data.model.skyblock.slayers.SlayerModel;
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
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.action.SelectMenu;
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

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        String guildName = commandContext.getArgument("name").flatMap(Argument::getValue).orElseThrow();
        ConcurrentList<SkillModel> skillModels = SimplifiedApi.getRepositoryOf(SkillModel.class).findAll();
        ConcurrentList<SlayerModel> slayerModels = SimplifiedApi.getRepositoryOf(SlayerModel.class).findAll().sorted(SortOrder.ASCENDING, Model::getId);
        DungeonModel catacombs = SimplifiedApi.getRepositoryOf(DungeonModel.class).findFirstOrNull(DungeonModel::getKey, "CATACOMBS");
        ConcurrentList<DungeonClassModel> dungeonClassModels = SimplifiedApi.getRepositoryOf(DungeonClassModel.class).findAll().sorted(SortOrder.ASCENDING, Model::getId);
        MojangData mojangData = SimplifiedApi.getWebApi(MojangData.class);
        HypixelSkyBlockData skyBlockData = SimplifiedApi.getWebApi(HypixelSkyBlockData.class);
        HypixelGuildResponse hypixelGuildResponse = SimplifiedApi.getWebApi(HypixelPlayerData.class).getGuildByName(guildName);

        if (hypixelGuildResponse.getGuild().isEmpty()) {
            return commandContext.reply(
            Response.builder()
                .withPages(
                    Page.builder()
                        .withEmbeds(
                            Embed.builder()
                                .withTitle("**Command Error**")
                                .withDescription("Invalid guild's name: " + guildName)
                                .withColor(Color.RED)
                                .build()
                        )
                        .build()
                )
                .build()
            );
        }

        HypixelGuildResponse.Guild guild = hypixelGuildResponse.getGuild().get();

        ConcurrentMap<MojangProfileResponse, SkyBlockIsland> guildMembers = guild.getMembers()
            .stream()
            .map(member -> Pair.of(
                mojangData.getProfileFromUniqueId(member.getUniqueId()),
                skyBlockData.getProfiles(member.getUniqueId()).getLastPlayed())
            )
            .collect(Concurrent.toMap());

        ConcurrentMap<UUID, String> ignMap = guildMembers.keySet().stream()
            .map(key -> Pair.of(key.getUniqueId(), key.getUsername()))
            .collect(Concurrent.toMap());
        ConcurrentList<SkyBlockIsland.Member> guildMemberPlayers = guildMembers.keySet().stream()
            .map(mojangProfileResponse -> guildMembers.get(mojangProfileResponse).getMember(mojangProfileResponse.getUniqueId()).orElseThrow())
            .collect(Concurrent.toList());

        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");

        Color tagColor;
        if (guild.getTagColor() == null) {
            tagColor = Color.YELLOW;
        } else {
            tagColor = guild.getTagColor().getColor();
        }
        String guildDescription;
        if (guild.getDescription() == null) {
            guildDescription = guildName + " doesn't have a description set.";
        } else {
            guildDescription = guild.getDescription();
        }
        String guildTag;
        if (guild.getTag() == null) {
            guildTag = "Tag was not found.";
        } else {
            guildTag = guild.getTag();
        }

        Response response = Response.builder()
            .withReference(commandContext)
            .isInteractable()
            .withTimeToLive(120)
            .withPages(Page.builder()
                .withPageItemStyle(PageItem.Style.FIELD_INLINE)
                .withItemsPerPage(20)
                .withOption(SelectMenu.Option.builder()
                    .withLabel("General Information")
                    .withDescription("Guild Averages and Totals")
                    .withValue("General Information")
                    .build())
                .withEmbeds(
                    Embed.builder()
                        .withTitle("**" + guildName + "**")
                        .withDescription(FormatUtil.format("""
                            {0}
                            Tag: {1}
                            Average Weight: **{2}** (Without Overflow: **{3}**)
                            Average Networth: **{4}**
                            """,
                            guildDescription,
                            guildTag,
                            guildMemberPlayers.stream().mapToInt(
                                guildMember -> (int) guildMember.getTotalWeight().getTotal()
                            ).sum() / guildMemberPlayers.size(),
                            guildMemberPlayers.stream().mapToInt(
                                guildMember -> (int) guildMember.getTotalWeight().getTotal() - (int) guildMember.getTotalWeight().getOverflow()
                            ).sum() / guildMemberPlayers.size(),
                            (long) guildMemberPlayers.stream().mapToDouble(
                                guildMember -> guildMember.getPurse() //TODO: add networth query
                            ).sum() / guildMemberPlayers.size()
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
                                .mapToDouble(SkyBlockIsland.Member::getSkillAverage).sum() / guildMemberPlayers.size()),
                            (long) guildMemberPlayers.stream()
                                .mapToDouble(SkyBlockIsland.Member::getSkillExperience).sum()
                        ))
                        .withOption(
                            SelectMenu.Option.builder()
                                .withLabel("**All Skills**")
                                .withValue("All Skills")
                                .build()
                        )
                        .build()
                )
                .withItems(
                    skillModels
                        .stream()
                        .map(skillModel -> PageItem.builder()
                            .withEmoji(Emoji.of(skillModel.getEmoji()))
                            .withData(FormatUtil.format(
                                """
                                    {0}Average Level: **{2}**
                                    {0}Total Experience:
                                    {1}**{3}**
                                    """,
                                emojiReplyStem,
                                emojiReplyEnd,
                                df.format(guildMemberPlayers.stream()
                                    .mapToDouble(member -> member.getSkill(skillModel).getLevel()).sum() / guildMemberPlayers.size()),
                                (long) guildMemberPlayers.stream()
                                    .mapToDouble(member -> member.getSkill(skillModel).getExperience()).sum()
                                )
                            )
                            .withOption(SelectMenu.Option.builder()
                                .withLabel("**" + skillModel.getName() + "**")
                                .withEmoji(Emoji.of(skillModel.getEmoji()))
                                .withValue(skillModel.getName())
                                .build()
                            )
                            .build()
                        )
                        .collect(Concurrent.toList())
                )
                .withItems(slayerModels.stream()
                    .map(slayerModel -> PageItem.builder()
                        .withEmoji(Emoji.of(slayerModel.getEmoji()))
                        .withData(FormatUtil.format(
                            """
                                {0}Average Level: **{2}**
                                {0}Total Experience:
                                {1}**{3}**
                                """,
                            emojiReplyStem,
                            emojiReplyEnd,
                            df.format(guildMemberPlayers.stream()
                                .mapToDouble(member -> member.getSlayer(slayerModel).getLevel()).sum() / guildMemberPlayers.size()),
                            (long) guildMemberPlayers.stream()
                                .mapToDouble(member -> member.getSlayer(slayerModel).getExperience()).sum()
                            )
                        )
                        .withOption(SelectMenu.Option.builder()
                            .withLabel("**" + slayerModel.getName() + "**")
                            .withEmoji(Emoji.of(slayerModel.getEmoji()))
                            .withValue(slayerModel.getName())
                            .build()
                        )
                        .build()
                    )
                    .collect(Concurrent.toList())
                )
                .withItems(PageItem.builder()
                    .withEmoji(Emoji.of(catacombs.getEmoji()))
                    .withData(FormatUtil.format(
                        """
                            {0}Average Level: **{2}**
                            {0}Total Experience:
                            {1}**{3}**
                            """,
                        emojiReplyStem,
                        emojiReplyEnd,
                        df.format(guildMemberPlayers.stream()
                            .mapToDouble(member -> member.getDungeons().getDungeon(catacombs).getLevel()).sum() / guildMemberPlayers.size()),
                        (long) guildMemberPlayers.stream()
                            .mapToDouble(member -> member.getDungeons().getDungeon(catacombs).getExperience()).sum()
                    )
                    )
                    .withOption(SelectMenu.Option.builder()
                        .withLabel("**Catacombs**")
                        .withEmoji(Emoji.of(catacombs.getEmoji()))
                        .withValue(catacombs.getName())
                        .build()
                    )
                    .build()
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
                    //.withEmoji(Emoji.of())
                    .build())
                .withEmbeds(
                    Embed.builder()
                        .withColor(tagColor)
                        .withTitle(guildName)
                        .withDescription(FormatUtil.format("""
                            Average Weight: **{0}** (Without Overflow: **{1}**)
                            """,
                            (int) (guildMemberPlayers.stream()
                                .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getTotalWeight().getTotal()).sum()
                                / guildMemberPlayers.size()),
                            (int) (guildMemberPlayers.stream()
                                .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getTotalWeight().getTotal() - guildMemberPlayer.getTotalWeight().getOverflow()).sum()
                                / guildMemberPlayers.size())
                            )
                        )
                        .build()
                )
                .withItems(
                    StreamUtil.mapWithIndex(
                        guildMemberPlayers.sorted(SortOrder.DESCENDING, guildMemberPlayer -> guildMemberPlayer.getTotalWeight().getTotal()).stream(),
                        (guildMemberPlayer, index, size) -> PageItem.builder()
                            .withValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                            .withLabel(FormatUtil.format(
                                " #{0} `{1}` >  **{2} [{3}]**",
                                index + 1,
                                ignMap.get(guildMemberPlayer.getUniqueId()),
                                (int) guildMemberPlayer.getTotalWeight().getTotal(),
                                (int) guildMemberPlayer.getTotalWeight().getTotal() - guildMemberPlayer.getTotalWeight().getOverflow()
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
                    //.withEmoji(Emoji.of())
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
                            .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getPurse()).sum() //TODO: networth query
                            / guildMemberPlayers.size()),
                            (long) (guildMemberPlayers.stream()
                                .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getPurse()).sum()))
                        )
                        .build()
                )
                .withItems(
                StreamUtil.mapWithIndex(
                        guildMemberPlayers.sorted(SortOrder.DESCENDING, guildMemberPlayer -> guildMemberPlayer.getPurse()).stream(),
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
            .withPages(skillModels.stream()
                .map(skillModel -> Page.builder()
                    .withPageItemStyle(PageItem.Style.SINGLE_COLUMN)
                    .withItemsPerPage(20)
                    .withOption(SelectMenu.Option.builder()
                        .withLabel(skillModel.getName() + " Leaderboard")
                        .withDescription("Guild Leaderboard for the " + skillModel.getName() + " Skill")
                        .withValue(skillModel.getName() + " Leaderboard")
                        .withEmoji(Emoji.of(skillModel.getEmoji()))
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
                                    .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getSkill(skillModel).getLevel()).sum()
                                    / guildMemberPlayers.size()),
                                skillModel.getMaxLevel()
                                )
                            )
                            .build()
                    )
                    .withItems(
                    StreamUtil.mapWithIndex(
                        guildMemberPlayers.sorted(SortOrder.DESCENDING, guildMemberPlayer -> guildMemberPlayer.getSkill(skillModel).getExperience()).stream(),
                        (guildMemberPlayer, index, size) -> PageItem.builder()
                            .withValue(ignMap.get(guildMemberPlayer.getUniqueId()))
                            .withLabel(FormatUtil.format(
                                " #{0} `{1}` >  **{2} [{3}]**",
                                index + 1,
                                ignMap.get(guildMemberPlayer.getUniqueId()),
                                (long) guildMemberPlayer.getSkill(skillModel).getExperience(),
                                guildMemberPlayer.getSkill(skillModel).getLevel()
                            ))
                            .build()
                    )
                        .collect(Concurrent.toList())
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
                        .withEmoji(Emoji.of(slayerModel.getEmoji()))
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
                                    .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getSlayer(slayerModel).getLevel()).sum() //TODO: check if slayer is null if isn't started?
                                    / guildMemberPlayers.size()),
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
            .withPages(Page.builder()
                .withPageItemStyle(PageItem.Style.SINGLE_COLUMN)
                .withItemsPerPage(20)
                .withOption(SelectMenu.Option.builder()
                    .withLabel("Catacombs Leaderboard")
                    .withDescription("Guild Leaderboard for Catacombs Level")
                    .withValue("Catacombs Leaderboard")
                    .withEmoji(Emoji.of(catacombs.getEmoji()))
                    .build())
                .withEmbeds(
                    Embed.builder()
                        .withColor(tagColor)
                        .withTitle(guildName)
                        .withDescription(FormatUtil.format("""
                            Catacombs Average: **{0}** / 50
                            """,
                            df.format(guildMemberPlayers.stream()
                                .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getDungeons().getDungeon(catacombs).getLevel()).sum()
                                / guildMemberPlayers.size())
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
                        .withEmoji(Emoji.of(classModel.getEmoji()))
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
                                    .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getDungeons().getClass(classModel).getLevel()).sum()
                                    / guildMemberPlayers.size()),
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
            .build();

        return commandContext.reply(response);
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            new Parameter(
                "name",
                "Name of the Guild to look up",
                Parameter.Type.TEXT,
                true
            ),
            new Parameter(
                "page",
                "Jump to a specific page",
                Parameter.Type.TEXT,
                false
            )
        );
    }
}
