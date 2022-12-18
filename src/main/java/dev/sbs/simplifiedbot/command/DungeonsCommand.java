package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.island.SkyBlockIsland;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeon_classes.DungeonClassModel;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeon_floors.DungeonFloorModel;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeons.DungeonModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@CommandInfo(
    id = "cc65f062-45f8-44c0-9635-84359e3ea246",
    name = "dungeons"
)
public class DungeonsCommand extends SkyBlockUserCommand {

    protected DungeonsCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull CommandContext<?> commandContext, @NotNull SkyBlockUser skyBlockUser) {
        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        MojangProfileResponse mojangProfile = skyBlockUser.getMojangProfile();
        SkyBlockIsland skyBlockIsland = skyBlockUser.getSelectedIsland();
        SkyBlockIsland.Member member = skyBlockUser.getMember();

        return commandContext.reply(
            Response.builder()
                .withTimeToLive(60)
                .isInteractable()
                .replyMention()
                .withReference(commandContext)
                .withPages(getDungeonPages(skyBlockUser, false))
                .withPages(getDungeonPages(skyBlockUser, true))
                .withPages(
                    Page.builder()
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("dungeon_classes")
                                .withLabel("Dungeon Classes")
                                .build()
                        )
                        .withEmbeds(
                            getSkillEmbed(
                                mojangProfile,
                                skyBlockIsland,
                                "dungeon_classes",
                                "Classes Information",
                                member.getDungeons().getClasses(),
                                member.getDungeonClassAverage(),
                                member.getDungeonClassExperience(),
                                member.getDungeonClassProgressPercentage(),
                                dungeonClass -> dungeonClass.getType().getName(),
                                dungeonClass -> Emoji.of(dungeonClass.getType().getEmoji()),
                                false
                            )
                                .mutate()
                                .withDescription(
                                    """
                                        {0}Selected Class: {2}
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
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

    private static @NotNull ConcurrentList<Page> getDungeonPages(@NotNull SkyBlockUser skyBlockUser, boolean masterMode) {
        String emojiReplyStem = getEmoji("REPLY_STEM").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(emoji -> FormatUtil.format("{0} ", emoji.asFormat())).orElse("");
        MojangProfileResponse mojangProfile = skyBlockUser.getMojangProfile();
        SkyBlockIsland skyBlockIsland = skyBlockUser.getSelectedIsland();
        SkyBlockIsland.Member member = skyBlockUser.getMember();
        Function<DungeonModel, String> identifierFunction = dungeonModel -> FormatUtil.format(
            "dungeon_{0}_{1}",
            dungeonModel.getKey(),
            (masterMode ? "master" : "_normal")
        );

        return SimplifiedApi.getRepositoryOf(DungeonModel.class)
            .matchAll(DungeonModel::isMasterModeEnabled)
            .stream()
            .map(dungeonModel -> Page.builder()
                .withOption(
                    SelectMenu.Option.builder()
                        .withValue(identifierFunction.apply(dungeonModel))
                        .withLabel(
                            "Dungeon: {0} ({1})",
                            dungeonModel.getName(),
                            (masterMode ? "Master" : "Normal")
                        )
                        .withEmoji(Emoji.of(dungeonModel.getEmoji()))
                        .isPlaceholderSelected()
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, identifierFunction.apply(dungeonModel), "Dungeon Information")
                        .withDescription(
                            """
                                {0}Total Runs: {2}
                                {1}Secrets Found: {3}
                                """,
                            emojiReplyStem,
                            emojiReplyEnd,
                            member.getDungeons()
                                .getDungeon(dungeonModel, masterMode)
                                .getTimesPlayed()
                                .stream()
                                .mapToInt(Map.Entry::getValue)
                                .sum(),
                            member.getDungeons()
                                .getDungeon(dungeonModel, masterMode)
                                .getBestRuns()
                                .stream()
                                .map(Map.Entry::getValue)
                                .flatMap(ConcurrentList::stream)
                                .mapToDouble(SkyBlockIsland.Dungeon.Run::getSecretsFound)
                                .sum()
                        )
                        .withFields(
                            Field.builder()
                                .withEmoji(Emoji.of(dungeonModel.getEmoji()))
                                .withName(dungeonModel.getName())
                                .withValue(
                                    """
                                        {0}Level: **{2}** / **{3}**
                                        {0}Experience: **{4,number,#,###}**
                                        {0}Progress to next Level: **{5,number,#.##}%**
                                        {1}Total Progress: **{6, number, #.##}%**
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    member.getDungeons().getDungeon(dungeonModel, masterMode).getLevel(),
                                    member.getDungeons().getDungeon(dungeonModel, masterMode).getMaxLevel(),
                                    member.getDungeons().getDungeon(dungeonModel, masterMode).getExperience(),
                                    member.getDungeons().getDungeon(dungeonModel, masterMode).getProgressPercentage(),
                                    member.getDungeons().getDungeon(dungeonModel, masterMode).getTotalProgressPercentage()
                                )
                                .build()
                        )
                        .withFields(
                            SimplifiedApi.getRepositoryOf(DungeonFloorModel.class).findAll()
                                .stream()
                                .map(dungeonFloorModel -> Field.builder()
                                    .withEmoji(Emoji.of(dungeonFloorModel.getFloorBoss().getEmoji()))
                                    .withName(dungeonFloorModel.getFloorBoss().getName())
                                    .withValue(
                                        """
                                            {0}Kills: **{2,number,#,###}**
                                            {0}Best Score: **{3,number,#}**
                                            {0}Best Time: **{4}**
                                            {0}Best S Tier: **{5}**
                                            {1}Best S+ Tier: **{6}**
                                            """,
                                        emojiReplyStem,
                                        emojiReplyEnd,
                                        member.getDungeons()
                                            .getDungeon(dungeonModel, masterMode)
                                            .getCompletions(dungeonFloorModel),
                                        member.getDungeons()
                                            .getDungeon(dungeonModel, masterMode)
                                            .getBestScore(dungeonFloorModel),
                                        getFastestDate(
                                            member.getDungeons()
                                                .getDungeon(dungeonModel, masterMode)
                                                .getFastestTime(dungeonFloorModel)
                                        ),
                                        getFastestDate(
                                            member.getDungeons()
                                                .getDungeon(dungeonModel, masterMode)
                                                .getFastestSTierTime(dungeonFloorModel)
                                        ),
                                        getFastestDate(
                                            member.getDungeons()
                                                .getDungeon(dungeonModel, masterMode)
                                                .getFastestSPlusTierTime(dungeonFloorModel)
                                        )
                                    )
                                    .isInline()
                                    .build()
                                )
                                .collect(Concurrent.toList())
                        )
                        .build()
                )
                .build()
            )
            .collect(Concurrent.toList());
    }

    private static String getFastestDate(long milliseconds) {
        if (milliseconds == 0)
            return "N/A";
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds), TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60);
    }

}
