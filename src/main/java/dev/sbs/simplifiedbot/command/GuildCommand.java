package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.collection.query.SortOrder;
import dev.sbs.api.tuple.pair.Pair;
import dev.sbs.api.util.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.DiscordCommand;
import dev.sbs.discordapi.command.Structure;
import dev.sbs.discordapi.command.parameter.Argument;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.component.interaction.SelectMenu;
import dev.sbs.discordapi.context.command.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.handler.Sorter;
import dev.sbs.discordapi.response.handler.item.ItemHandler;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.item.field.StringItem;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.client.hypixel.HypixelClient;
import dev.sbs.minecraftapi.client.hypixel.request.HypixelEndpoint;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelGuild;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelPlayer;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelPlayerResponse;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.SkyBlockIsland;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.SkyBlockMember;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.member.dungeon.DungeonClass;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.member.dungeon.DungeonEntry;
import dev.sbs.minecraftapi.client.hypixel.response.skyblock.member.skill.SkillEntry;
import dev.sbs.minecraftapi.model.Skill;
import dev.sbs.minecraftapi.model.Slayer;
import dev.sbs.minecraftapi.render.text.ChatFormat;
import dev.sbs.minecraftapi.skyblock.common.Weight;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;

@Structure(
    name = "guild",
    description = "Lookup a skyblock guild"
)
public class GuildCommand extends DiscordCommand<SlashCommandContext> {

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

        HypixelGuildResponse hypixelGuildResponse = SimplifiedApi.getClient(HypixelClient.class).getEndpoint().getGuildByName(guildName);

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

        ConcurrentList<Skill> skillModels = MinecraftApi.getRepository(Skill.class).findAll();
        ConcurrentList<Slayer> slayerModels = MinecraftApi.getRepository(Slayer.class).findAll();
        DungeonEntry.Type catacombs = DungeonEntry.Type.CATACOMBS;
        ConcurrentList<DungeonClass.Type> dungeonClassModels = Concurrent.newList(DungeonClass.Type.HEALER, DungeonClass.Type.MAGE, DungeonClass.Type.BERSERK, DungeonClass.Type.ARCHER, DungeonClass.Type.TANK);
        HypixelEndpoint hypixelEndpoints = SimplifiedApi.getClient(HypixelClient.class).getEndpoint();
        HypixelGuild guild = hypixelGuildResponse.getGuild().get();

        ConcurrentMap<HypixelPlayer, SkyBlockIsland> guildMembers = guild.getMembers()
            .stream()
            .limit(2) // TODO: limiting size!!
            .map(HypixelGuild.Member::getUniqueId)
            .map(hypixelEndpoints::getPlayer)
            .map(HypixelPlayerResponse::getPlayer)
            .flatMap(Optional::stream)
            .map(player -> Pair.of(
                player,
                hypixelEndpoints.getProfiles(player.getUniqueId()).getSelected()
            ))
            .collect(Concurrent.toMap());

        ConcurrentMap<UUID, String> ignMap = guildMembers.keySet().stream()
            .map(key -> Pair.of(key.getUniqueId(), key.getDisplayName()))
            .collect(Concurrent.toMap());

        ConcurrentList<SkyBlockMember> guildMemberPlayers = guildMembers.keySet().stream()
            .map(hypixelPlayer -> guildMembers.get(hypixelPlayer)
                .getMembers()
                .get(hypixelPlayer.getUniqueId())
            )
            .collect(Concurrent.toList());

        ConcurrentMap<SkyBlockMember, Weight> totalWeights = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, guildMemberPlayer.getTotalWeight()))
            .collect(Concurrent.toMap());

        ConcurrentMap<SkyBlockMember, Long> networths = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(guildMemberPlayer, (long) guildMemberPlayer.getCurrencies().getPurse())) //TODO: networth query
            .collect(Concurrent.toMap());

        ConcurrentMap<SkyBlockMember, ConcurrentList<SkillEntry>> skills = guildMemberPlayers.stream()
            .map(guildMemberPlayer -> Pair.of(
                guildMemberPlayer,
                guildMemberPlayer.getSkills().getSkills()
            ))
            .collect(Concurrent.toMap());

        ConcurrentMap<String, Emoji> emojis = new ConcurrentMap<>();
        skillModels.forEach(skillModel -> this.getEmoji("SKILL_" + skillModel.getId()).ifPresent(emoji -> emojis.put(skillModel.getId(), emoji)));
        slayerModels.forEach(slayerModel -> this.getEmoji("SLAYER_" + slayerModel.getId()).ifPresent(emoji -> emojis.put(slayerModel.getId(), emoji)));
        this.getEmoji("DUNGEON_CATACOMBS").ifPresent(emoji -> emojis.put(catacombs.name(), emoji));
        dungeonClassModels.forEach(classType -> this.getEmoji("CLASS_" + classType.name()).ifPresent(emoji -> emojis.put(classType.name(), emoji)));
        emojis.put("skills", this.getEmoji("SKILLS").orElseThrow());
        emojis.put("weight", this.getEmoji("WEIGHT").orElseThrow());
        emojis.put("networth", this.getEmoji("TRADING_COIN").orElseThrow());
        emojis.put("skyblock", this.getEmoji("SKYBLOCK").orElseThrow());
        emojis.put("slayer", this.getEmoji("SLAYER").orElseThrow());

        String emojiReplyStem = this.getEmoji("REPLY_STEM").map(Emoji::asSpacedFormat).orElse("");
        String emojiReplyEnd = this.getEmoji("REPLY_END").map(Emoji::asSpacedFormat).orElse("");

        Color tagColor = guild.getTagColor().orElse(ChatFormat.YELLOW).getColor();
        String guildDescription = guild.getDescription().orElse(guild.getName() + " doesn't have a description set.");
        String guildTag = guild.getTag().orElse("Tag was not found.");
        int guildLevel = guild.getLevel();
        String guildOwner = ignMap.get(guild.getGuildMaster().getUniqueId());
        String pageIdentifier = commandContext.getArgument("page").map(Argument::asString).orElse("general_information");

        return commandContext.reply(Response.builder()
            //.isInteractable()
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
                                    .mapToDouble(member -> member.getSkills().getAverage()).average().orElseThrow()),
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
                                ItemHandler.<SkyBlockMember>embed()
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        Sorter.<SkyBlockMember>builder()
                                            .withFunctions(member -> member.getLeveling().getExperience())
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer(
                                        (guildMemberPlayer, index, size) -> StringItem.builder()
                                            .withDescription(
                                                "%s. `%s` > **%s**",
                                                index + 1,
                                                ignMap.get(guildMemberPlayer.getUniqueId()),
                                                guildMemberPlayer.getLeveling().getLevel()
                                            )
                                            .build()
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
                            .withEmbeds(
                                Embed.builder()
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
                        ItemHandler.<Skill>embed()
                            .withItems(skillModels)
                            .withTransformer((skillModel, index, size) -> StringItem.builder()
                                .withEmoji(emojis.get(skillModel.getId()))
                                .withDescription(
                                    """
                                        %1$sAverage Level: **%3$s**
                                        %1$sTotal Experience:
                                        %2$s**%4$s**
                                        """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    df.format(guildMemberPlayers.stream()
                                                  .mapToDouble(member -> skills.get(member).stream().filter(skill -> skill.getId().equalsIgnoreCase(skillModel.getId()))
                                                      .findFirst().orElseThrow().getLevel()).average().orElseThrow()),
                                    (long) guildMemberPlayers.stream()
                                        .mapToDouble(member -> skills.get(member).stream().filter(skill -> skill.getId().equalsIgnoreCase(skillModel.getId()))
                                            .findFirst().orElseThrow().getExperience()).sum()
                                )
                                .withOption(
                                    getOptionBuilder(skillModel.getId().toLowerCase())
                                        .withEmoji(emojis.get(skillModel.getId()))
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
                                ItemHandler.<SkyBlockMember>embed()
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        Sorter.<SkyBlockMember>builder()
                                            .withFunctions(guildMemberPlayer -> skills.get(guildMemberPlayer)
                                                .stream()
                                                .filter(skill -> skill.getId().equalsIgnoreCase(skillModel.getId()))
                                                .findFirst()
                                                .orElseThrow()
                                                .getExperience()
                                            )
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer(
                                        (member, index, size) -> StringItem.builder()
                                            .withDescription(
                                                "%s. `%s` >  **%s [%s]**",
                                                index + 1,
                                                ignMap.get(member.getUniqueId()),
                                                (long) skills.get(member)
                                                    .stream()
                                                    .filter(skill -> skill.getId().equalsIgnoreCase(skillModel.getId()))
                                                    .findFirst()
                                                    .orElseThrow()
                                                    .getExperience(),
                                                skills.get(member)
                                                    .stream()
                                                    .filter(skill -> skill.getId().equalsIgnoreCase(skillModel.getId()))
                                                    .findFirst()
                                                    .orElseThrow()
                                                    .getLevel()
                                            )
                                            .build()
                                    )
                                    .withListTitle(skillModel.getName() + " Leaderboard")
                                    .withAmountPerPage(20)
                                    .build()
                            )
                            .withOption(
                                getOptionBuilder(skillModel.getId().toLowerCase() + "_leaderboard")
                                    .withDescription("Guild Leaderboard for the " + skillModel.getName() + " Skill")
                                    .withEmoji(emojis.get(skillModel.getId()))
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
                                                .filter(skill -> skill.getId().equalsIgnoreCase(skillModel.getId()))
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
                        ItemHandler.<Slayer>embed()
                            .withItems(slayerModels)
                            .withTransformer((slayerModel, index, size) -> StringItem.builder()
                                .withEmoji(emojis.get(slayerModel.getId()))
                                .withDescription(
                                    """
                                    %1$sAverage Level: **%3$s**
                                    %1$sTotal Experience:
                                    %2$s**%4$s**
                                    """,
                                    emojiReplyStem,
                                    emojiReplyEnd,
                                    df.format(guildMemberPlayers.stream()
                                                  .mapToDouble(member -> member.getSlayers().getSlayer(slayerModel.getId()).getLevel()).average().orElseThrow()),
                                    (long) guildMemberPlayers.stream()
                                        .mapToDouble(member -> member.getSlayers().getSlayer(slayerModel.getId()).getExperience()).sum()
                                )
                                .withOption(
                                    getOptionBuilder(slayerModel.getName().replace(" ", "_").toLowerCase())
                                        .withEmoji(emojis.get(slayerModel.getId()))
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
                                    ItemHandler.<SkyBlockMember>embed()
                                        //.withColumnNames(Triple.of(slayerModel.getName() + " Leaderboard", "", ""))
                                        .withItems(guildMemberPlayers)
                                        .withSorters(
                                            Sorter.<SkyBlockMember>builder()
                                                .withFunctions(guildMemberPlayer -> guildMemberPlayer.getSlayers().getSlayer(slayerModel.getId()).getExperience())
                                                .withOrder(SortOrder.DESCENDING)
                                                .build()
                                        )
                                        .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                            .withValue(String.format(
                                                "%s. `%s` >  **%s [%s]**",
                                                index + 1,
                                                ignMap.get(guildMemberPlayer.getUniqueId()),
                                                (long) guildMemberPlayer.getSlayers().getSlayer(slayerModel.getId()).getExperience(),
                                                guildMemberPlayer.getSlayers().getSlayer(slayerModel.getId()).getLevel()
                                            ))
                                            .build()
                                        )
                                        .withAmountPerPage(20)
                                        .build()
                                )
                                .withOption(
                                    getOptionBuilder(slayerModel.getName().replace(" ", "_").toLowerCase() + "_leaderboard")
                                        .withDescription("Guild Leaderboard for the " + slayerModel.getName() + " Slayer")
                                        .withEmoji(emojis.get(slayerModel.getId()))
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
                                                .mapToDouble(guildMemberPlayer -> guildMemberPlayer.getSlayers().getSlayer(slayerModel.getId()).getLevel()).average().orElseThrow()),
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
                        ItemHandler.<DungeonClass.Type>embed()
                            .withItems(dungeonClassModels)
                            .withTransformer((dungeonClassModel, index, size) -> StringItem.builder()
                                .withEmoji(emojis.get(dungeonClassModel.name()))
                                .withDescription(String.format(
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
                                    getOptionBuilder(dungeonClassModel.name().toLowerCase())
                                        .withEmoji(emojis.get(dungeonClassModel.name()))
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
                            .withEmoji(emojis.get(catacombs.name()))
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
                                ItemHandler.<SkyBlockMember>embed()
                                    .withListTitle("Catacombs Leaderboard")
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        Sorter.<SkyBlockMember>builder()
                                            .withFunctions(guildMemberPlayer -> guildMemberPlayer.getDungeons().getDungeon(catacombs).getExperience())
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                        .withDescription(String.format(
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
                                    .withEmoji(emojis.get(catacombs.name()))
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
                                ItemHandler.<SkyBlockMember>embed()
                                    .withListTitle("%s Leaderboard", classModel.getName())
                                    .withItems(guildMemberPlayers)
                                    .withSorters(
                                        Sorter.<SkyBlockMember>builder()
                                            .withFunctions(guildMemberPlayer -> guildMemberPlayer.getDungeons().getClass(classModel).getExperience())
                                            .withOrder(SortOrder.DESCENDING)
                                            .build()
                                    )
                                    .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                        .withDescription(String.format(
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
                                getOptionBuilder(classModel.name().toLowerCase() + "_leaderboard")
                                    .withDescription("Guild Leaderboard for the " + classModel.getName() + " Class")
                                    .withEmoji(emojis.get(classModel.name()))
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
                        ItemHandler.<SkyBlockMember>embed()
                            .withListTitle("Weight Leaderboard")
                            .withItems(guildMemberPlayers)
                            .withSorters(
                                Sorter.<SkyBlockMember>builder()
                                    .withFunctions(guildMemberPlayer -> totalWeights.get(guildMemberPlayer).getTotal())
                                    .withOrder(SortOrder.DESCENDING)
                                    .build()
                            )
                            .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                .withDescription(String.format(
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
                        ItemHandler.<SkyBlockMember>embed()
                            .withListTitle("Networth Leaderboard")
                            .withItems(guildMemberPlayers)
                            .withSorters(
                                Sorter.<SkyBlockMember>builder()
                                    .withFunctions(networths::get)
                                    .withOrder(SortOrder.DESCENDING)
                                    .build()
                            )
                            .withTransformer((guildMemberPlayer, index, size) -> StringItem.builder()
                                .withDescription(String.format(
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
