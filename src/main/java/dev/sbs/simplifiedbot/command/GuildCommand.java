package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.implementation.HypixelPlayerData;
import dev.sbs.api.client.hypixel.implementation.HypixelSkyBlockData;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.hypixel.response.skyblock.island.playerstats.PlayerStats;
import dev.sbs.api.client.sbs.implementation.MojangData;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.skyblock.skills.SkillModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.PageItem;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;


@CommandInfo(
    id = "b04d133d-3532-447b-8782-37d1036f3957",
    name = "guild"
)
public class GuildCommand extends Command {

    protected GuildCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        String guildName = commandContext.getArgument("name").get().getValue().get(); //should be fine to assume present cuz guild is marked as required arg?
        Map<String, SkillModel> skillModels = SimplifiedApi.getRepositoryOf(SkillModel.class).findAll()
            .stream()
            .map(skillModel -> Pair.of(
                skillModel.getName(),
                skillModel
            ))
            .collect(Concurrent.toMap());
        SkillModel combatSkillModel = SimplifiedApi.getRepositoryOf(SkillModel.class).findFirstOrNull(SkillModel::getKey, "COMBAT");
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
                                .withColor(Color.YELLOW)
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

        ConcurrentList<SkyBlockIsland.Member> guildMemberPlayers = guild.getMembers().stream()
            .map(member -> skyBlockData.getProfiles(member.getUniqueId()).getLastPlayed().getMember(member.getUniqueId()).get())
            .collect(Concurrent.toList());

        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");

        Response response = Response.builder()
            .withReference(commandContext)
            .withPages(
                Page.builder()
                    .withEmbeds(
                        Embed.builder()
                            .withTitle("**" + guildName + "**")
                            .withDescription(guild.getDescription() + "\nTag: " + guild.getTag())
                            .build()
                    )
                    .withItems(
                        PageItem.builder()
                            .withLabel("**All Skills**")
                            .withData(FormatUtil.format(
                                """
                                    {0}Average Level: **{2}**
                                    {1}Total Experience: **{3}**
                                    """,
                                emojiReplyStem,
                                emojiReplyEnd,
                                guildMemberPlayers.stream()
                                    .mapToDouble(SkyBlockIsland.Member::getSkillAverage).sum() / guildMemberPlayers.size(),
                                guildMemberPlayers.stream()
                                    .mapToDouble(SkyBlockIsland.Member::getSkillExperience).sum()
                            ))
                            .build()
                    )
                    .withItems(
                        skillModels.values()
                            .stream()
                            .map(skillModel -> PageItem.builder()
                                .withLabel("**" + skillModel.getName() + "**")
                                .withEmoji(Emoji.of(skillModel.getEmoji()))
                                .withData(FormatUtil.format(
                                        """
                                            {0}Average Level: **{2}** / **{3}**
                                            {1}Total Experience: **{4}**
                                            """,
                                        emojiReplyStem,
                                        emojiReplyEnd,
                                        guildMemberPlayers.stream()
                                            .mapToDouble(member -> member.getSkill(skillModel).getLevel()).sum() / guildMemberPlayers.size(),
                                        guildMemberPlayers.stream()
                                            .mapToDouble(member -> member.getSkill(skillModel).getMaxLevel()),
                                        guildMemberPlayers.stream()
                                            .mapToDouble(member -> member.getSkill(skillModel).getExperience()).sum()
                                    )
                                )
                                .build()
                            )
                            .collect(Collectors.toList())
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
