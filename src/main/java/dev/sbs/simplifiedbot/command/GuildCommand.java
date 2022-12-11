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
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.PageItem;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

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
        SkillModel combatSkillModel = SimplifiedApi.getRepositoryOf(SkillModel.class).findFirstOrNull(SkillModel::getKey, "COMBAT");
        MojangData mojangData = SimplifiedApi.getWebApi(MojangData.class);
        HypixelSkyBlockData skyBlockData = SimplifiedApi.getWebApi(HypixelSkyBlockData.class);
        HypixelGuildResponse hypixelGuildResponse = SimplifiedApi.getWebApi(HypixelPlayerData.class).getGuildByName("SkyBlock Simplified");

        if (hypixelGuildResponse.getGuild().isPresent()) {
            HypixelGuildResponse.Guild guild = hypixelGuildResponse.getGuild().get();

            ConcurrentMap<MojangProfileResponse, SkyBlockIsland> guildMembers = guild.getMembers()
                .stream()
                .map(member -> Pair.of(
                    mojangData.getProfileFromUniqueId(member.getUniqueId()),
                    skyBlockData.getProfiles(member.getUniqueId()).getLastPlayed())
                )
                .collect(Concurrent.toMap());

            Response.builder()
                .withReference(commandContext)
                .withPages(
                    Page.builder()
                        .withItems(
                            guildMembers.stream()
                                .flatMap(member -> member.getValue()
                                    .getMember(member.getKey().getUniqueId())
                                    .stream()
                                    .map(skyBlockMember -> skyBlockMember.getSkill(combatSkillModel).getLevel())
                                    .map(combatLevel -> PageItem.builder()
                                        .withLabel("Combat")
                                        .withValue(String.valueOf(combatLevel))
                                        .build()
                                    )
                                )
                                .collect(Concurrent.toList())
                        )
                        .build()
                )
                .build();
        }

        return commandContext.reply(
            Response.builder()
                .withReference(commandContext)
                .withPages(
                    Page.builder()
                        .withContent("guild command")
                        .build()
                )
                .build()
        );
    }

}
