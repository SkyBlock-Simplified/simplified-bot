package dev.sbs.simplifiedbot.command.group.developer;

import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.ListUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.util.DiscordObject;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.command.DevCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

@CommandInfo(
    id = "2ce215ee-1d4e-4c7a-bb77-82f6c39eb02d",
    name = "stats",
    parent = DevCommand.class
)
public class DevStatsCommand extends Command {

    protected DevStatsCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected Mono<Void> process(CommandContext<?> commandContext) throws DiscordException {
        Optional<Snowflake> optionalGuildId = Optional.empty();

        // Handle DMs
        if (ListUtil.isEmpty(commandContext.getArguments())) {
            if (!commandContext.isPrivateChannel())
                optionalGuildId = commandContext.getGuild().map(Guild::getId).blockOptional();
        } else
            optionalGuildId = commandContext.getArguments().get(0).getValue().map(Snowflake::of);

        Embed.EmbedBuilder embedBuilder = Embed.builder()
            .withTimestamp(Instant.now())
            .withColor(Color.DARK_GRAY);

        optionalGuildId.flatMap(guildId -> this.getDiscordBot()
            .getGateway()
            .getGuildById(guildId)
            .blockOptional()
        ).ifPresentOrElse(guild -> {
            String emojiReplyStem = getEmoji("REPLY_STEM").map(Emoji::asFormat).orElse("");
            String emojiReplyEnd = getEmoji("REPLY_END").map(Emoji::asFormat).orElse("");
            ConcurrentList<Channel> channels = guild.getChannels().toStream().collect(Concurrent.toList());
            boolean animatedIcon = guild.getData().icon().map(value -> value.startsWith("a_")).orElse(false);

            embedBuilder.withAuthor("Server Information", getEmoji("STATUS_INFO").map(Emoji::getUrl))
                .withFooter(
                    guild.getVanityUrlCode().map(vanityCode -> FormatUtil.format("https://discord.gg/{0}", vanityCode)).orElse(""),
                    this.getDiscordBot().getMainGuild().getIconUrl(discord4j.rest.util.Image.Format.GIF)
                )
                .withTitle("Server :: {0}", guild.getName())
                .withDescription(guild.getDescription())
                .withThumbnailUrl(guild.getIconUrl(animatedIcon ? discord4j.rest.util.Image.Format.GIF : discord4j.rest.util.Image.Format.PNG))
                .withField(
                    "About",
                    FormatUtil.format(
                        """
                        {0}Owner: {2}
                        {0}Created: <t:{3,number,#}:D>
                        {0}Members: {4} / {5}
                        {0}Roles: {6}
                        {1}Channels: {7}
                        """,
                        emojiReplyStem,
                        emojiReplyEnd,
                        guild.getOwner().map(Member::getMention).blockOptional().orElse("Unknown"),
                        guild.getId().getTimestamp().getEpochSecond(),
                        this.getDiscordBot().getGateway().getRestClient().restGuild(guild.getData()).getData().blockOptional().flatMap(guildUpdateData -> guildUpdateData.approximatePresenceCount().toOptional()).orElse(0),
                        guild.getMemberCount(),
                        guild.getRoles().toStream().collect(Concurrent.toList()).size(),
                        channels.size()
                    )
                )
                .withField(
                    "Security",
                    FormatUtil.format(
                        """
                        {0}Verification Level: {2}
                        {0}Content Filter: {3}
                        {0}Default Notifications: {4}
                        {1}Two-Factor Authentication: {5}
                        """,
                        emojiReplyStem,
                        emojiReplyEnd,
                        capitalizeEnum(guild.getVerificationLevel()),
                        capitalizeEnum(guild.getContentFilterLevel()),
                        capitalizeEnum(guild.getNotificationLevel()),
                        capitalizeEnum(guild.getMfaLevel())
                    )
                );

            if (ListUtil.notEmpty(guild.getFeatures())) {
                embedBuilder.withField(
                    "Features",
                    StringUtil.join(
                        guild.getFeatures()
                            .stream()
                            .map(DiscordObject::capitalizeFully)
                            .collect(Concurrent.toList()),
                        ", "
                    )
                );
            }
        }, () -> embedBuilder.withDescription("This isn't a Guild."));

        return commandContext.reply(
            Response.builder()
                .isInteractable(false)
                .withReference(commandContext)
                .withEmbeds(embedBuilder.build())
                .build()
        );
    }

}
