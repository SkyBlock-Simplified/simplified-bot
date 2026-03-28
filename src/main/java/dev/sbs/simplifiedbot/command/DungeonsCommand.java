package dev.sbs.simplifiedbot.command;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Structure;
import dev.sbs.discordapi.component.interaction.SelectMenu;
import dev.sbs.discordapi.context.command.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.TreePage;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.SkyBlockIsland;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.SkyBlockMember;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.member.dungeon.DungeonEntry;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.member.dungeon.Floor;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.member.dungeon.FloorData;
import dev.sbs.minecraftapi.client.mojang.response.MojangProfile;
import dev.sbs.simplifiedbot.util.SkyBlockUser;
import dev.sbs.simplifiedbot.util.SkyBlockUserCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Structure(
    name = "dungeons",
    description = "Lookup a players dungeon stats"
)
public class DungeonsCommand extends SkyBlockUserCommand {

    protected DungeonsCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> subprocess(@NotNull SlashCommandContext commandContext, @NotNull SkyBlockUser skyBlockUser) {
        String emojiReplyStem = this.getEmoji("REPLY_STEM").map(Emoji::asSpacedFormat).orElse("");
        String emojiReplyEnd = this.getEmoji("REPLY_END").map(Emoji::asSpacedFormat).orElse("");
        MojangProfile mojangProfile = skyBlockUser.getMojangProfile();
        SkyBlockIsland skyBlockIsland = skyBlockUser.getSelectedIsland();
        SkyBlockMember member = skyBlockUser.getMember();

        return commandContext.reply(
            Response.builder()
                .withTimeToLive(60)
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
                                member.getDungeons().getClassAverage(),
                                member.getDungeons().getClassExperience(),
                                member.getDungeons().getClassProgressPercentage(),
                                dungeonClass -> dungeonClass.getType().getName(),
                                dungeonClass -> Emoji.of(dungeonClass.getType().getEmoji()),
                                false
                            )
                                .mutate()
                                .withDescription(
                                    """
                                        %1$sSelected Class: %3$s
                                        %1$sAverage Class Level: %4$.2f
                                        %1$sTotal Class Experience: %5$.2f
                                        %2$sTotal Class Progress: %6$.2f%%
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    member.getDungeons()
                                        .getSelectedClass()
                                        .getName(),
                                    member.getDungeons().getClassAverage(),
                                    member.getDungeons().getClassExperience(),
                                    member.getDungeons().getClassProgressPercentage()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

    private @NotNull ConcurrentList<TreePage> getDungeonPages(@NotNull SkyBlockUser skyBlockUser, boolean masterMode) {
        String emojiReplyStem = this.getEmoji("REPLY_STEM").map(Emoji::asSpacedFormat).orElse("");
        String emojiReplyEnd = this.getEmoji("REPLY_END").map(Emoji::asSpacedFormat).orElse("");
        MojangProfile mojangProfile = skyBlockUser.getMojangProfile();
        SkyBlockIsland skyBlockIsland = skyBlockUser.getSelectedIsland();
        SkyBlockMember member = skyBlockUser.getMember();
        Function<DungeonEntry.Type, String> identifierFunction = dungeonType -> String.format(
            "dungeon_%s_%s",
            dungeonType.name(),
            (masterMode ? "MASTER" : "NORMAL")
        );

        return member.getDungeons()
            .getDungeons()
            .stream()
            .collapseToSingle((dungeonType, dungeon) -> Page.builder()
                .withOption(
                    SelectMenu.Option.builder()
                        .withValue(identifierFunction.apply(dungeonType))
                        .withLabel(
                            "Dungeon: %s (%s)",
                            dungeonType.getName(),
                            (masterMode ? "Master" : "Normal")
                        )
                        // TODO: DungeonEntry.Type has no emoji field
                        .build()
                )
                .withEmbeds(
                    getEmbedBuilder(mojangProfile, skyBlockIsland, identifierFunction.apply(dungeonType))
                        .withDescription(
                            """
                                %1$sTotal Runs: %3$s
                                %2$sSecrets Found: %4$s
                                """,
                            emojiReplyStem,
                            emojiReplyEnd,
                            dungeon.getFloorData(masterMode)
                                .getTimesPlayed()
                                .stream()
                                .mapToInt(Map.Entry::getValue)
                                .sum(),
                            dungeon.getFloorData(masterMode)
                                .getBestRuns()
                                .stream()
                                .map(Map.Entry::getValue)
                                .flatMap(ConcurrentList::stream)
                                .mapToDouble(FloorData.BestRun::getSecretsFound)
                                .sum()
                        )
                        .withFields(
                            Field.builder()
                                // TODO: DungeonEntry.Type has no emoji field
                                .withName(dungeonType.getName())
                                .withValue(
                                    """
                                        %1$sLevel: **%3$s** / **%4$s**
                                        %1$sExperience: **%5$,f**
                                        %1$sProgress to next Level: **%6$.2f%%**
                                        %2$sTotal Progress: **%7$.2f%%**
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    dungeon.getLevel(),
                                    dungeon.getMaxLevel(),
                                    dungeon.getExperience(),
                                    dungeon.getProgressPercentage(),
                                    dungeon.getTotalProgressPercentage()
                                )
                                .build()
                        )
                        .withFields(
                            Arrays.stream(Floor.values())
                                .map(floor -> Field.builder()
                                    // TODO: Floor.Boss has no emoji field
                                    .withName(floor.getBoss().getName())
                                    .withValue(
                                        """
                                            %1$sKills: **%3$d**
                                            %1$sBest Score: **%4$d**
                                            %1$sBest Time: **%5$s**
                                            %1$sBest S Tier: **%6$s**
                                            %2$sBest S+ Tier: **%7$s**
                                            """,
                                        emojiReplyStem,
                                        emojiReplyEnd,
                                        dungeon.getFloorData(masterMode)
                                            .getCompletions()
                                            .get(floor.getValue()),
                                        dungeon.getFloorData(masterMode)
                                            .getBestScore()
                                            .get(floor.getValue()),
                                        getFastestDate(
                                            dungeon.getFloorData(masterMode)
                                                .getFastestTime()
                                                .get(floor.getValue())
                                        ),
                                        getFastestDate(
                                            dungeon.getFloorData(masterMode)
                                                .getFastestSTierTime()
                                                .get(floor.getValue())
                                        ),
                                        getFastestDate(
                                            dungeon.getFloorData(masterMode)
                                                .getFastestSPlusTierTime()
                                                .get(floor.getValue())
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
