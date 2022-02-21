package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.sbs_developers.SbsDeveloperModel;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.Field;
import dev.sbs.discordapi.response.page.Page;

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
    protected void process(CommandContext<?> commandContext) {
        commandContext.reply(
            Response.builder()
                .replyMention()
                .withReference(commandContext)
                .isInteractable()
                .withTimeToLive(60)
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
                            Field.of(
                                "Owner",
                                "<@154743493464555521>",
                                true
                            ),
                            Field.of(
                                "Developers",
                                StringUtil.join(
                                    SimplifiedApi.getRepositoryOf(SbsDeveloperModel.class)
                                        .findAll()
                                        .stream()
                                        .map(sbsDeveloperModel -> FormatUtil.format("<@{0,number,#}>", sbsDeveloperModel.getDiscordId()))
                                        .collect(Concurrent.toList()),
                                    "\n"
                                ),
                                true
                            ),
                            Field.of(
                                "Version",
                                "1.5 Beta"
                            )
                        )
                        .withFields(
                            Field.of(
                                "Support Server",
                                "https://discord.gg/sbs",
                                true
                            ),
                            Field.empty(),
                            Field.of(
                                "Invite Bot",
                                "https://sbs.dev/bot/invite",
                                true
                            )
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
                    Page.create()
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
                    Page.create()
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
        );
    }

}
