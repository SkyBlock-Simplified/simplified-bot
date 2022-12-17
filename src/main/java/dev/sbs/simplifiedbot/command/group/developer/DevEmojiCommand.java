package dev.sbs.simplifiedbot.command.group.developer;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.emojis.EmojiModel;
import dev.sbs.api.data.model.discord.emojis.EmojiSqlModel;
import dev.sbs.api.data.model.discord.emojis.EmojiSqlRepository;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildModel;
import dev.sbs.api.data.model.discord.guild_data.guilds.GuildSqlModel;
import dev.sbs.api.util.helper.WordUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;
import discord4j.common.util.Snowflake;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Objects;

@CommandInfo(
    id = "1287dda9-ded3-4c7e-9e42-33bfd79043f8",
    name = "emoji",
    parent = DevCommand.class
)
public class DevEmojiCommand extends Command {

    protected DevEmojiCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) throws DiscordException {
        return Mono.fromRunnable(() -> SimplifiedApi.getRepositoryOf(GuildModel.class)
            .matchAll(GuildModel::isEmojiServer)
            .sorted(GuildModel::getId)
            .forEach(guildModel -> this.getDiscordBot()
                .getGateway()
                .getGuildById(Snowflake.of(guildModel.getGuildId()))
                .blockOptional()
                .ifPresent(guild -> guild.getEmojis()
                    .toStream()
                    .forEach(guildEmoji -> {
                        String key = guildEmoji.getName().toUpperCase();
                        String name = WordUtil.capitalizeFully(guildEmoji.getName().replace("_", " "));
                        EmojiSqlModel existingEmojiModel = SimplifiedApi.getRepositoryOf(EmojiSqlModel.class).findFirstOrNull(EmojiModel::getKey, key);

                        if (Objects.nonNull(existingEmojiModel)) {
                            if (existingEmojiModel.getEmojiId() != guildEmoji.getId().asLong()) {
                                existingEmojiModel.setEmojiId(guildEmoji.getId().asLong());

                                ((EmojiSqlRepository) SimplifiedApi.getRepositoryOf(EmojiSqlModel.class)).update(existingEmojiModel);
                                this.getLog().info("Updating emoji: {0} :: {1,number,#} :: {2}", guild.getName(), guildEmoji.getId().asLong(), name);
                            }
                        } else {
                            EmojiSqlModel newEmojiModel = new EmojiSqlModel();
                            newEmojiModel.setEmojiId(guildEmoji.getId().asLong());
                            newEmojiModel.setGuild((GuildSqlModel) guildModel);
                            newEmojiModel.setKey(key);
                            newEmojiModel.setName(name);
                            newEmojiModel.setAnimated(guildEmoji.isAnimated());
                            ((EmojiSqlRepository) SimplifiedApi.getRepositoryOf(EmojiSqlModel.class)).save(newEmojiModel);
                            this.getLog().info("Saving new emoji: {0} :: {1,number,#} :: {2}", guild.getName(), guildEmoji.getId().asLong(), name);
                        }
                    })
                )
            ));
    }

}
