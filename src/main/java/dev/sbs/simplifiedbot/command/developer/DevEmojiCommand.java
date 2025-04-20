package dev.sbs.simplifiedbot.command.developer;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.emojis.EmojiModel;
import dev.sbs.api.data.model.discord.emojis.EmojiSqlModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildSqlModel;
import dev.sbs.api.data.sql.SqlRepository;
import dev.sbs.api.util.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.DiscordCommand;
import dev.sbs.discordapi.command.Structure;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.exception.DiscordException;
import discord4j.common.util.Snowflake;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Structure(
    parent = "dev",
    name = "emoji"
)
public class DevEmojiCommand extends DiscordCommand<SlashCommandContext> {

    protected DevEmojiCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        return Mono.fromRunnable(() -> SimplifiedApi.getRepositoryOf(GuildModel.class)
            .matchAll(GuildModel::isEmojiServer)
            //.sorted(GuildModel::getSubmittedAt)
            .forEach(guildModel -> this.getDiscordBot()
                .getGateway()
                .getGuildById(Snowflake.of(guildModel.getGuildId()))
                .blockOptional()
                .ifPresent(guild -> guild.getEmojis()
                    .toStream()
                    .forEach(guildEmoji -> {
                        String key = guildEmoji.getName().toUpperCase();
                        String name = StringUtil.capitalizeFully(guildEmoji.getName().replace("_", " "));
                        EmojiSqlModel existingEmojiModel = SimplifiedApi.getRepositoryOf(EmojiSqlModel.class).findFirstOrNull(EmojiModel::getKey, key);

                        if (Objects.nonNull(existingEmojiModel)) {
                            if (existingEmojiModel.getEmojiId() != guildEmoji.getId().asLong()) {
                                existingEmojiModel.setEmojiId(guildEmoji.getId().asLong());

                                ((SqlRepository<EmojiSqlModel>) SimplifiedApi.getRepositoryOf(EmojiSqlModel.class)).update(existingEmojiModel);
                                this.getLog().info("Updating emoji: {} :: {} :: {}", guild.getName(), guildEmoji.getId().asLong(), name);
                            }
                        } else {
                            EmojiSqlModel newEmojiModel = new EmojiSqlModel();
                            newEmojiModel.setEmojiId(guildEmoji.getId().asLong());
                            newEmojiModel.setGuild((GuildSqlModel) guildModel);
                            newEmojiModel.setKey(key);
                            newEmojiModel.setName(name);
                            newEmojiModel.setAnimated(guildEmoji.isAnimated());
                            ((SqlRepository<EmojiSqlModel>) SimplifiedApi.getRepositoryOf(EmojiSqlModel.class)).save(newEmojiModel);
                            this.getLog().info("Saving new emoji: {} :: {} :: {}", guild.getName(), guildEmoji.getId().asLong(), name);
                        }
                    })
                )
            ));
    }

}
