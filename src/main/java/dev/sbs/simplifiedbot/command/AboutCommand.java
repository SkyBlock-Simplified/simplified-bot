package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.sbs_developers.SbsDeveloperModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;

@CommandInfo(
    id = "1b072a71-2045-457a-a9f7-354b936567cb",
    name = "about"
)
public class AboutCommand extends Command {

    protected AboutCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        return commandContext.reply(
            Response.builder()
                .replyMention()
                .withReference(commandContext)
                .isInteractable()
                .withTimeToLive(60)
                .withPages(
                    Page.builder()
                        .withOption(
                            SelectMenu.Option.builder()
                                .isDefault()
                                .withValue("about")
                                .withLabel("About")
                                .withDescription("General information about the bot.")
                                .build()
                        )
                        .withEmbeds(
                            Embed.builder()
                                .withAuthor("Bot Information", getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                .withTitle("About :: General")
                                .withTimestamp(Instant.now())
                                .withColor(Color.DARK_GRAY)
                                .withDescription(
                                    "The official SkyBlock Simplified bot. Optimize your gear with `/optimizer`, " +
                                        "lookup guilds with `/guild`, lookup data on players such as general details, " +
                                        "net worth, skills, dungeons, slayers and more with `/player`. Look for hypixel " +
                                        "reports on players like scamlist, macros, and irl trading using `/report`. Add and " +
                                        "receive reputation with `/rep`, host giveaways, and much more."
                                )
                                .withFields(
                                    Field.builder()
                                        .withName("Owner")
                                        .withValue("<@154743493464555521>")
                                        .isInline()
                                        .build(),
                                    Field.builder()
                                        .withName("Developers")
                                        .withValue(
                                            StringUtil.join(
                                                SimplifiedApi.getRepositoryOf(SbsDeveloperModel.class)
                                                    .stream()
                                                    .map(sbsDeveloperModel -> FormatUtil.format("<@{0,number,#}>", sbsDeveloperModel.getDiscordId()))
                                                    .collect(Concurrent.toList()),
                                                "\n"
                                            )
                                        )
                                        .build(),
                                    Field.builder()
                                        .withName("Version")
                                        .withValue("1.5 Beta")
                                        .isInline()
                                        .build()
                                )
                                .withFields(
                                    Field.builder()
                                        .withName("Support Server")
                                        .withValue("https://discord.gg/sbs")
                                        .isInline()
                                        .build(),
                                    Field.empty(),
                                    Field.builder()
                                        .withName("Bot Invite Link")
                                        .withValue("https://sbs.dev/bot/invite")
                                        .isInline()
                                        .build()
                                )
                                .withField(
                                    "Patreon",
                                    "Paying for Patreon comes with perks available on the SkyBlock Simplified Discord, Bot, Mod, Website and API. " +
                                        "The full details of available perks can be seen on the Patreon page in the menu below.\n\n" +
                                        "Sign up here: N/A"
                                )
                                .build()
                        )
                        .withPages(
                            Page.builder()
                                .withOption(
                                    SelectMenu.Option.builder()
                                        .withValue("donors")
                                        .withLabel("donors")
                                        .withDescription("All legacy donors of the SkyBlock Simplified discord.")
                                        .build()
                                )
                                .withEmbeds(
                                    Embed.builder()
                                        .withAuthor("Bot Information", getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                        .withTitle("About :: Donors")
                                        .withTimestamp(Instant.now())
                                        .withColor(Color.DARK_GRAY)
                                        .withDescription("A big thank you goes out to those who donated early on in the development of the SkyBlock Simplified discord and bot!")
                                        .build()
                                )
                                .withItems(
                                    // TODO
                                )
                                .build(),
                            Page.builder()
                                .withOption(
                                    SelectMenu.Option.builder()
                                        .withValue("patreon")
                                        .withLabel("patreon")
                                        .withDescription("Details and list of perks available for Patreon users.")
                                        .build()
                                )
                                .withEmbeds(
                                    Embed.builder()
                                        .withAuthor("Bot Information", getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                        .withTitle("About :: Patreon")
                                        .withTimestamp(Instant.now())
                                        .withColor(Color.DARK_GRAY)
                                        .withDescription("Perks will be listed here as they become available. The Patreon system is not live yet.")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

}
