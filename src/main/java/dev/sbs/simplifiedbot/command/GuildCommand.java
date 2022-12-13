package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.implementation.HypixelPlayerData;
import dev.sbs.api.client.hypixel.implementation.HypixelSkyBlockData;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.sbs.implementation.MojangData;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.skyblock.skills.SkillModel;
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
import java.util.Comparator;
import java.util.Map;
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
        String guildName = commandContext.getArgument("name").flatMap(Argument::getValue).orElseThrow(); //should be fine to assume present cuz guild is marked as required arg?
        Map<String, SkillModel> skillModels = SimplifiedApi.getRepositoryOf(SkillModel.class).findAll()
            .stream()
            .map(skillModel -> Pair.of(
                skillModel.getKey(),
                skillModel
            ))
            .collect(Concurrent.toMap());
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
            .map(mojangProfileResponse -> guildMembers.get(mojangProfileResponse).getMember(mojangProfileResponse.getUniqueId()).get())
            .collect(Concurrent.toList());

        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");

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
                        .withDescription(
                            guild.getDescription() + "\nTag: " + guild.getTag() +
                                "\nAverage Weight: **" +
                                guildMemberPlayers.stream().mapToInt(
                                    guildMember -> (int) guildMember.getTotalWeight().getTotal()
                                ).sum() / guildMemberPlayers.size() +
                                "** (Without Overflow: **" +
                                guildMemberPlayers.stream().mapToInt(
                                    guildMember -> (int) guildMember.getTotalWeight().getTotal() - (int) guildMember.getTotalWeight().getOverflow()
                                ).sum() / guildMemberPlayers.size() +
                                "**)" +
                                "\nAverage Networth: **" +
                                guildMemberPlayers.stream().mapToLong(
                                    //guildMember -> guildMember.getNetworth() //TODO: add networth query
                                    guildMember -> 10
                                ).sum() / guildMemberPlayers.size() +
                                "**"
                        )
                        .withColor(guild.getTagColor().getColor())
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
                    skillModels.values()
                        .stream()
                        .map(skillModel -> PageItem.builder()
                            .withData(FormatUtil.format(
                                    """
                                        {0}Average Level: **{2}** / **{3}**
                                        {0}Total Experience:
                                        {1}**{4}**
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    (int) (guildMemberPlayers.stream()
                                        .mapToDouble(member -> member.getSkill(skillModel).getLevel()).sum() / guildMemberPlayers.size()),
                                    skillModel.getMaxLevel(),
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
//                    .withItems(
//                        guildMembers.stream()
//                            .flatMap(member -> member.getValue()
//                                .getMember(member.getKey().getUniqueId())
//                                .stream()
//                                .map(skyBlockMember -> skyBlockMember.getSkill(combatSkillModel).getLevel())
//                                .map(combatLevel -> PageItem.builder()
//                                    .withLabel("Combat")
//                                    .withValue(String.valueOf(combatLevel))
//                                    .build()
//                                )
//                            )
//                            .collect(Concurrent.toList())
//                    )
                .build()
            )
            .withPages(skillModels.values().stream()
                    .map(skillModel -> Page.builder()
                            .withPageItemStyle(PageItem.Style.SINGLE_COLUMN)
                            .withItemsPerPage(20)
                            .withOption(SelectMenu.Option.builder()
                                .withLabel(skillModel.getName() + " Leaderboard")
                                .withDescription("Guild Leaderboard for the " + skillModel.getName() + " Skill")
                                .withValue(skillModel.getName() + " Leaderboard")
                                .build())
                            .withEmbeds(
                                Embed.builder()
                                    .withColor(guild.getTagColor().getColor())
                                    .withTitle(guildName)
                                    .withDescription(
                                        skillModel.getName() + " Average: " + df.format(guildMemberPlayers.stream()
                                            .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getSkill(skillModel).getLevel()).sum()
                                            / guildMemberPlayers.size()) + " / " + skillModel.getMaxLevel()
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
//                                            .withOption(SelectMenu.Option.builder()
//                                                .withLabel("")
//                                                .withValue(ignMap.get(guildMemberPlayer.getUniqueId()))
//                                                .build()
//                                            )
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
            )
        );
    }
}
