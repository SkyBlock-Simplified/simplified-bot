package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.impl.hypixel.request.HypixelRequest;
import dev.sbs.api.client.impl.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.api.client.impl.hypixel.response.hypixel.implementation.HypixelGuild;
import dev.sbs.api.client.impl.hypixel.response.hypixel.implementation.HypixelPlayer;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.SkyBlockIsland;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.Slayer;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.member.EnhancedMember;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.util.skill.Skill;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.island.util.weight.Weight;
import dev.sbs.api.data.model.Model;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeon_classes.DungeonClassModel;
import dev.sbs.api.data.model.skyblock.dungeon_data.dungeons.DungeonModel;
import dev.sbs.api.data.model.skyblock.skills.SkillModel;
import dev.sbs.api.data.model.skyblock.slayers.SlayerModel;
import dev.sbs.api.minecraft.text.ChatFormat;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.collection.sort.SortOrder;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.api.util.mutable.pair.Pair;
import dev.sbs.api.util.mutable.triple.Triple;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.command.parameter.Argument;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.handler.ItemHandler;
import dev.sbs.discordapi.response.page.handler.sorter.Sorter;
import dev.sbs.discordapi.response.page.item.field.StringItem;
import dev.sbs.simplifiedbot.util.SqlSlashCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.UUID;

@CommandId("b04d133d-3532-447b-8782-37d1036f3957")
public class GuildCommand extends SqlSlashCommand {

    protected GuildCommand(@NotNull DiscordBot discordBot) {
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
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) {
        String guildName = commandContext.getArgument("name").map(Argument::asString).orElseThrow();

        HypixelGuildResponse hypixelGuildResponse = SimplifiedApi.getApiRequest(HypixelRequest.class).getGuildByName(guildName);

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
        HypixelRequest hypixelRequest = SimplifiedApi.getApiRequest(HypixelRequest.class);
        HypixelGuild guild = hypixelGuildResponse.getGuild().get();

        ConcurrentMap<HypixelPlayer, SkyBlockIsland> guildMembers = guild.getMembers()
            .stream()
            .limit(2) // TODO: limiting size!!
            .map(member -> Pair.of(
                hypixelRequest.getPlayer(member.getUniqueId()).getPlayer(),
                hypixelRequest.getProfiles(member.getUniqueId()).getSelected())
            )
            .collect(Concurrent.toMap());

        ConcurrentMap<UUID, String> ignMap = guildMembers.keySet().stream()
            .map(key -> Pair.of(key.getUniqueId(), key.getDisplayName()))
            .collect(Concurrent.toMap());

        ConcurrentList<EnhancedMember> guildMemberPlayers = guildMembers.keySet().stream()
            .map(hypixelPlayer -> guildMembers.get(hypixelPlayer)
                .getMembers()
                .get(hypixelPlayer.getUniqueId())
                .asEnhanced()
            )
            .collect(Concurrent.toList());

        ConcurrentMap<EnhancedMember, Weight> totalWeights = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getTotalWeight()))
            .collect(Concurrent.toMap());

        ConcurrentMap<EnhancedMember, Double> networths = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getCurrencies().getPurse())) //TODO: networth query
            .collect(Concurrent.toMap());

        ConcurrentMap<EnhancedMember, ConcurrentList<Skill>> skills = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getPlayerData().getSkills()))
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

        String emojiReplyStem = getEmoji("REPLY_STEM").map(Emoji::asSpacedFormat).orElse("");
        String emojiReplyEnd = getEmoji("REPLY_END").map(Emoji::asSpacedFormat).orElse("");

        Color tagColor = guild.getTagColor().orElse(ChatFormat.YELLOW).getColor();
        String guildDescription = guild.getDescription().orElse(guild.getName() + " doesn't have a description set.");
        String guildTag = guild.getTag().orElse("Tag was not found.");
        int guildLevel = guild.getLevel();
        String guildOwner = ignMap.get(guild.getGuildMaster().getUniqueId());
        String pageIdentifier = commandContext.getArgument("page").map(Argument::asString).orElse("general_information");

        return commandContext.reply(Response.builder()
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
                                %s
                                
                                Average Weight: **%s** (Without Overflow: **%s**)
                                Average Networth: **%s**
                                Average Skill Level: **%s**
                                Average Skyblock Level: **%s**
                                """,
                                guildDescription,
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getTotal()
                                ).average().orElse(0),
                                (int) guildMemberPlayers.stream().mapToDouble(
                                    guildMember -> totalWeights.get(guildMember).getValue()
                                ).average().orElse(0),
                                (long) guildMemberPlayers.stream().mapToDouble(
                                    networths::get
                                ).average().orElse(0),
                                df.format(guildMemberPlayers.stream()
                                    .mapToDouble(EnhancedMember::getSkillAverage).average().orElseThrow()),
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
                            .withItemHandler(
                            ItemHandler.builder(EnhancedMember.class)
                                .withItems(guildMemberPlayers)
                                .withSorters(
                                    Sorter.<EnhancedMember>builder()
                                        .withFunctions(member -> member.getLeveling().getExperience())
                                        .withOrder(SortOrder.DESCENDING)
                                        .build()
                                )
                                .withTransformer(
                                    String.class,
                                    (guildMemberPlayer, index, size) -> String.format(
                                        "%s. `%s` > **%s**",
                                        index + 1,
                                        ignMap.get(guildMemberPlayer.getUniqueId()),
                                        guildMemberPlayer.getLeveling().getLevel()
                                    )
                                )
                                .withListTransformer(
                                    (guildMemberPlayer, index, size) -> String.format(
                                        "%s. `%s` > **%s**",
                                        index + 1,
                                        ignMap.get(guildMemberPlayer.getUniqueId()),
                                        guildMemberPlayer.getLeveling().getLevel()
                                    )
                                )
                                .withListTitle("SkyBlock Level Leaderboard")
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
                                    Skyblock Level Average: **%s**
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
                    .build(),
                Page.builder()
                    .withItemHandler(
                        ItemHandler.builder(SkillModel.class)
                            .withItems(skillModels)
                            .withTransformer((skillModel, index, size) -> StringItem.builder()
                                .withEmoji(emojis.get(skillModel.getKey()))
                                .withData(
                                    """
                                        %1$sAverage Level: **%3$s**
                                        %1$sTotal Experience:
                                        %2$s**%4$s**
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
                                .withOption(
                                    getOptionBuilder(skillModel.getKey().toLowerCase())
                                        .withEmoji(emojis.get(skillModel.getKey()))
                                        .build()
                                )
                                .build()
                            )
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
                                Average Weight: **%s** (Without Overflow: **%s**)
                                Average Networth: **%s**
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
                            .withItemHandler(
                                ItemHandler.builder(EnhancedMember.class)
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        Sorter.<EnhancedMember>builder()
                                            .withFunctions(guildMemberPlayer -> skills.get(guildMemberPlayer)
                                                .stream()
                                                .filter(skill -> skill.getType().name().equalsIgnoreCase(skillModel.getKey()))
                                                .findFirst()
                                                .orElseThrow()
                                                .getExperience()
                                            )
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer(
                                        (member, index, size) -> String.format(
                                            "%s. `%s` >  **%s [%s]**",
                                            index + 1,
                                            ignMap.get(member.getUniqueId()),
                                            (long) skills.get(member)
                                                .stream()
                                                .filter(skill -> skill.getType().name().equalsIgnoreCase(skillModel.getKey()))
                                                .findFirst()
                                                .orElseThrow()
                                                .getExperience(),
                                            skills.get(member)
                                                .stream()
                                                .filter(skill -> skill.getType().name().equalsIgnoreCase(skillModel.getKey()))
                                                .findFirst()
                                                .orElseThrow()
                                                .asEnhanced(member.getJacobsContest())
                                                .getLevel()
                                        )
                                    )
                                    .withListTitle(skillModel.getName() + " Leaderboard")
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
                                    .withDescription(
                                        """
                                        %s Average: **%s** / %s
                                        """,
                                        skillModel.getName(),
                                        df.format(guildMemberPlayers.stream().mapToDouble(guildMemberPlayer -> skills.get(guildMemberPlayer)
                                                .stream()
                                                .filter(skill -> skill.getType().name().equalsIgnoreCase(skillModel.getKey()))
                                                .findFirst()
                                                .orElseThrow()
                                                .getLevel()
                                            ).average().orElseThrow()),
                                        skillModel.getMaxLevel()
                                    )
                                    .build()
                            )
                            .build()
                        )
                        .collect(Concurrent.toList())
                    )
                    .build(),
                Page.builder()
                    .withItemHandler(
                        ItemHandler.builder(SlayerModel.class)
                            .withItems(slayerModels)
                            .withTransformer((slayerModel, index, size) -> StringItem.builder()
                                .withEmoji(emojis.get(slayerModel.getKey()))
                                .withData(
                                    """
                                    %1$sAverage Level: **%3$s**
                                    %1$sTotal Experience:
                                    %2$s**%4$s**
                                    """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    df.format(guildMemberPlayers.stream()
                                                  .mapToDouble(member -> member.getSlayer(slayerModel).getLevel()).average().orElseThrow()),
                                    (long) guildMemberPlayers.stream()
                                        .mapToDouble(member -> member.getSlayer(slayerModel).getExperience()).sum()
                                )
                                .withOption(
                                    getOptionBuilder(slayerModel.getName().replace(" ", "_").toLowerCase())
                                        .withEmoji(emojis.get(slayerModel.getKey()))
                                        .build()
                                )
                                .build()
                            )
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
                                Average Weight: **%s** (Without Overflow: **%s**)
                                Average Networth: **%s**
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
                                .withItemHandler(
                                    ItemHandler.builder(EnhancedMember.class)
                                        .withColumnNames(Triple.of(slayerModel.getName() + " Leaderboard", "", ""))
                                        .withItems(guildMemberPlayers)
                                        .withSorters(
                                            ItemHandler.Sorter.<EnhancedMember>builder()
                                                .withFunctions(guildMemberPlayer -> guildMemberPlayer.getSlayer().getBoss(Slayer.Type.of(slayerModel.getKey())).getExperience())
                                                .withOrder(SortOrder.DESCENDING)
                                                .build()
                                        )
                                        .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                            .withData(String.format(
                                                "%s. `%s` >  **%s [%s]**",
                                                index + 1,
                                                ignMap.get(guildMemberPlayer.getUniqueId()),
                                                (long) guildMemberPlayer.getSlayer(slayerModel).getExperience(),
                                                guildMemberPlayer.getSlayer(slayerModel).getLevel()
                                            ))
                                            .build()
                                        )
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
                                            %s Average: **%s** / %s
                                            """,
                                            slayerModel.getName(),
                                            df.format(guildMemberPlayers.stream()
                                                .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getSlayer(slayerModel).getLevel()).average().orElseThrow()),
                                            9 // TODO: Get max level
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .collect(Concurrent.toList())
                    )
                    .build(),
                Page.builder()
                    .withItemHandler(
                        ItemHandler.builder(DungeonClassModel.class)
                            .withItems(dungeonClassModels)
                            .withTransformer((dungeonClassModel, index, size) -> StringItem.builder()
                                .withEmoji(emojis.get(dungeonClassModel.getKey()))
                                .withData(String.format(
                                    """
                                    %1$sAverage Level: **%3$s**
                                    %1$sTotal Experience:
                                    %2$s**%4$s**
                                    """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    df.format(guildMemberPlayers.stream()
                                                  .mapToDouble(member -> member.getDungeons().getClass(dungeonClassModel).getLevel()).average().orElseThrow()),
                                    (long) guildMemberPlayers.stream()
                                        .mapToDouble(member -> member.getDungeons().getClass(dungeonClassModel).getExperience()).sum()
                                ))
                                .withOption(
                                    getOptionBuilder(dungeonClassModel.getKey().toLowerCase())
                                        .withEmoji(emojis.get(dungeonClassModel.getKey()))
                                        .build()
                                )
                                .build()
                            )
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
                                Average Weight: **%s** (Without Overflow: **%s**)
                                Average Networth: **%s**
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
                            .withItemHandler(
                                ItemHandler.builder(EnhancedMember.class)
                                    .withColumnNames(Triple.of("Catacombs Leaderboard", "", ""))
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        ItemHandler.Sorter.<EnhancedMember>builder()
                                            .withFunctions(guildMemberPlayer -> guildMemberPlayer.getDungeons().getDungeon(catacombs).getExperience())
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                        .withData(String.format(
                                            " #%s `%s` >  **%s [%s]**",
                                            index + 1,
                                            ignMap.get(guildMemberPlayer.getUniqueId()),
                                            (long) guildMemberPlayer.getDungeons().getDungeon(catacombs).getExperience(),
                                            guildMemberPlayer.getDungeons().getDungeon(catacombs).getLevel()
                                        ))
                                        .build()
                                    )
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
                                        Catacombs Average: **%s** / 50
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
                            .withItemHandler(
                                ItemHandler.builder(EnhancedMember.class)
                                    .withColumnNames(Triple.of(classModel.getName() + " Leaderboard", "", ""))
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        ItemHandler.Sorter.<EnhancedMember>builder()
                                            .withFunctions(guildMemberPlayer -> guildMemberPlayer.getDungeons().getClass(classModel).getExperience())
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                        .withData(String.format(
                                            "%s. `%s` >  **%s [%s]**",
                                            index + 1,
                                            ignMap.get(guildMemberPlayer.getUniqueId()),
                                            (long) guildMemberPlayer.getDungeons().getClass(classModel).getExperience(),
                                            guildMemberPlayer.getDungeons().getClass(classModel).getLevel()
                                        ))
                                        .build()
                                    )
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
                                        %s Average: **%s** / %s
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
                    .build(),
                Page.builder()
                    .withItemHandler(
                        ItemHandler.builder(EnhancedMember.class)
                            .withColumnNames(Triple.of("Weight Leaderboard", "", ""))
                            .withItems(guildMemberPlayers)
                            .withSorters(
                                ItemHandler.Sorter.<EnhancedMember>builder()
                                    .withFunctions(guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getTotal())
                                    .withOrder(SortOrder.DESCENDING)
                                    .build()
                            )
                            .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                .withData(String.format(
                                    " #%s `%s` >  **%s [%s]**",
                                    index + 1,
                                    ignMap.get(guildMemberPlayer.getUniqueId()),
                                    (int) totalWeights.get(guildMemberPlayer).getTotal(),
                                    (int) totalWeights.get(guildMemberPlayer).getValue()
                                ))
                                .build()
                            )
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
                                Average Weight: **%s** (Without Overflow: **%s**)
                                """,
                                (int) (guildMemberPlayers.stream()
                                    .mapToDouble(guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getTotal()).average().orElseThrow()),
                                (int) (guildMemberPlayers.stream()
                                    .mapToDouble(guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getValue()).average().orElseThrow())
                            )
                            .build()
                    )
                    .build(),
                Page.builder()
                    .withItemHandler(
                        ItemHandler.builder(EnhancedMember.class)
                            .withColumnNames(Triple.of("Networth Leaderboard", "", ""))
                            .withItems(guildMemberPlayers)
                            .withSorters(
                                ItemHandler.Sorter.<EnhancedMember>builder()
                                    .withFunctions(networths::get)
                                    .withOrder(SortOrder.DESCENDING)
                                    .build()
                            )
                            .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                .withData(String.format(
                                    "%s. `%s` >  **%s**",
                                    index + 1,
                                    ignMap.get(guildMemberPlayer.getUniqueId()),
                                    (int) guildMemberPlayer.getCurrencies().getPurse()
                                ))
                                .build()
                            )
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
                            .withDescription(
                                """
                                    Average Networth: **%s**
                                    Total Networth: **%s**""",
                                (long) (guildMemberPlayers.stream()
                                    .mapToDouble(networths::get).average().orElseThrow()),
                                (long) (guildMemberPlayers.stream()
                                    .mapToDouble(networths::get).sum())
                            )
                            .build()
                    )
                    .build()
            )
            .withDefaultPage(pageIdentifier)
            .build()
        );
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder()
                .withName("name")
                .withDescription("Name of the Guild to look up.")
                .withType(Parameter.Type.TEXT)
                .isRequired()
                .build(),
            Parameter.builder()
                .withName("page")
                .withDescription("Jump to a specific page.")
                .withType(Parameter.Type.TEXT)
                .withChoices(
                    pageIdentifiers.stream()
                        .map(pageIdentifier -> Pair.of(StringUtil.capitalizeFully(pageIdentifier.replace("_", " ")), pageIdentifier))
                        .collect(Concurrent.toWeakLinkedMap())
                )
                .build()
        );
    }

    private static SelectMenu.Option.Builder getOptionBuilder(String identifier) {
        return SelectMenu.Option.builder()
            .withValue(identifier)
            .withLabel(StringUtil.capitalizeFully(identifier.replace("_", " ")));
    }
}
