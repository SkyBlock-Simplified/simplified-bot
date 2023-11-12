package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.Dungeon;
import dev.sbs.api.client.hypixel.response.skyblock.implementation.island.SkyBlockIsland;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeon_classes.DungeonClassModel;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeon_floors.DungeonFloorModel;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeons.DungeonModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.context.interaction.deferrable.application.slash.SlashCommandContext;
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

@CommandId("cc65f062-45f8-44c0-9635-84359e3ea246")
public class DungeonsCommand extends SkyBlockUserCommand {

    protected DungeonsCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull SlashCommandContext commandContext, @NotNull SkyBlockUser skyBlockUser) {
        String emojiReplyStem = getEmoji("REPLY_STEM").map(Emoji::asSpacedFormat).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(Emoji::asSpacedFormat).orElse("");
        MojangProfileResponse mojangProfile = skyBlockUser.getMojangProfile();
        SkyBlockIsland skyBlockIsland = skyBlockUser.getSelectedIsland();
        SkyBlockIsland.Member member = skyBlockUser.getMember();

        return commandContext.reply(
            Response.builder()
                .withTimeToLive(60)
                .isInteractable()
                .replyMention()
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
                                member.getDungeons().getClasses(),
                                member.getDungeonClassAverage(),
                                member.getDungeonClassExperience(),
                                member.getDungeonClassProgressPercentage(),
                                dungeonClass -> dungeonClass.getType().getName(),
                                dungeonClass -> Emoji.of(dungeonClass.getType().getEmoji()),
                                false
                            )
                                .mutate()
                                .withDescription(String.format(
                                    """
                                        %1$sSelected Class: %3$s
                                        %1$sAverage Class Level: {3,number,#.##}
                                        %1$sTotal Class Experience: {4,number,#.##}
                                        %2$sTotal Class Progress: {5,number,#.##}%
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    member.getDungeons().getSelectedClassModel().map(DungeonClassModel::getName).orElse(getEmojiAsFormat("TEXT_NULL", "*<null>*")),
                                    member.getDungeonClassAverage(),
                                    member.getDungeonClassExperience(),
                                    member.getDungeonClassProgressPercentage()
                                ))
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

    private static @NotNull ConcurrentList<Page> getDungeonPages(@NotNull SkyBlockUser skyBlockUser, boolean masterMode) {
        String emojiReplyStem = getEmoji("REPLY_STEM").map(Emoji::asSpacedFormat).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(Emoji::asSpacedFormat).orElse("");
        MojangProfileResponse mojangProfile = skyBlockUser.getMojangProfile();
        SkyBlockIsland skyBlockIsland = skyBlockUser.getSelectedIsland();
        SkyBlockIsland.Member member = skyBlockUser.getMember();
        Function<DungeonModel, String> identifierFunction = dungeonModel -> String.format(
            "dungeon_%s_%s",
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
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, identifierFunction.apply(dungeonModel))
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
                                .mapToDouble(Dungeon.BestRun::getSecretsFound)
                                .sum()
                        )
                        .withFields(
                            Field.builder()
                                .withEmoji(Emoji.of(dungeonModel.getEmoji()))
                                .withName(dungeonModel.getName())
                                .withValue(
                                    """
                                        %1$sLevel: **%3$s** / **%4$s**
                                        %1$sExperience: **%5$,f**
                                        %1$sProgress to next Level: **%6$.2f%%**
                                        %2$sTotal Progress: **%7$.2f%%**
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
