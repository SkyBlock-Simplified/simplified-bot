package dev.sbs.simplifiedbot.command.developer;

import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.ListUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.command.parameter.Argument;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.interaction.deferrable.application.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.structure.Author;
import dev.sbs.discordapi.response.embed.structure.Footer;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.util.base.DiscordHelper;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.util.SqlSlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

@CommandId("2ce215ee-1d4e-4c7a-bb77-82f6c39eb02d")
public class DevStatsCommand extends SqlSlashCommand {

    protected DevStatsCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder("guild", "Discord Guild Name", Parameter.Type.TEXT)
                .build()
        );
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) throws DiscordException {
        Optional<Snowflake> optionalGuildId = Optional.empty();

        // Handle DMs
        if (ListUtil.isEmpty(commandContext.getArguments())) {
            if (!commandContext.isPrivateChannel())
                optionalGuildId = commandContext.getGuild().map(Guild::getId).blockOptional();
        } else
            optionalGuildId = commandContext.getArgument("guild").map(Argument::asSnowflake);

        Embed.Builder builder = Embed.builder()
            .withColor(Color.DARK_GRAY)
            .withFooter(
                Footer.builder()
                    .withTimestamp(Instant.now())
                    .build()
            );

        optionalGuildId.flatMap(guildId -> this.getDiscordBot()
            .getGateway()
            .getGuildById(guildId)
            .blockOptional()
        ).ifPresentOrElse(guild -> {
            String emojiReplyStem = getEmoji("REPLY_STEM").map(Emoji::asFormat).orElse("");
            String emojiReplyEnd = getEmoji("REPLY_END").map(Emoji::asFormat).orElse("");
            ConcurrentList<Channel> channels = guild.getChannels().toStream().collect(Concurrent.toList());
            boolean animatedIcon = guild.getData().icon().map(value -> value.startsWith("a_")).orElse(false);

            builder.withAuthor(
                    Author.builder()
                        .withName("Server Information")
                        .withIconUrl(getEmoji("STATUS_INFO").map(Emoji::getUrl))
                        .build()
                )
                .withFooter(
                    Footer.builder()
                        .withText(guild.getVanityUrlCode().map(vanityCode -> String.format("https://discord.gg/%s", vanityCode)).orElse(""))
                        .withIconUrl(this.getDiscordBot().getMainGuild().getIconUrl(discord4j.rest.util.Image.Format.GIF))
                        .build()
                )
                .withTitle("Server :: %s", guild.getName())
                .withDescription(guild.getDescription())
                .withThumbnailUrl(guild.getIconUrl(animatedIcon ? discord4j.rest.util.Image.Format.GIF : discord4j.rest.util.Image.Format.PNG))
                .withField(
                    "About",
                    String.format(
                        """
                        %1$sOwner: %3$s
                        %1$sCreated: <t:%4$s:D>
                        %1$sMembers: %5$s / %6$s
                        %1$sRoles: %7$s
                        %2$sChannels: %8$s
                        """,
                        emojiReplyStem,
                        emojiReplyEnd,
                        guild.getOwner().map(Member::getMention).blockOptional().orElse("Unknown"),
                        guild.getId().getTimestamp().getEpochSecond(),
                        this.getDiscordBot()
                            .getGateway()
                            .getRestClient()
                            .restGuild(guild.getData())
                            .getData()
                            .blockOptional()
                            .flatMap(guildUpdateData -> guildUpdateData.approximatePresenceCount().toOptional())
                            .orElse(0),
                        guild.getMemberCount(),
                        guild.getRoles().toStream().collect(Concurrent.toList()).size(),
                        channels.size()
                    )
                )
                .withField(
                    "Security",
                    String.format(
                        """
                        %1$sVerification Level: %3$s
                        %1$sContent Filter: %4$s
                        %1$sDefault Notifications: %5$s
                        %2$sTwo-Factor Authentication: %6$s
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
                builder.withField(
                    "Features",
                    StringUtil.join(
                        guild.getFeatures()
                            .stream()
                            .map(DiscordHelper::capitalizeFully)
                            .collect(Concurrent.toList()),
                        ", "
                    )
                );
            }
        }, () -> builder.withDescription("This isn't a Guild."));

        return commandContext.reply(
            Response.builder()
                .isNotInteractable()
                .withPages(
                    Page.builder()
                        .withEmbeds(builder.build())
                        .build()
                )
                .build()
        );
    }

}
