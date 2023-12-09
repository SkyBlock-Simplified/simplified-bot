package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.sbs_data.sbs_legacy_donors.SbsLegacyDonorModel;
import dev.sbs.api.data.model.discord.users.UserModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandId;
import dev.sbs.discordapi.context.deferrable.application.SlashCommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.component.interaction.action.SelectMenu;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.structure.Author;
import dev.sbs.discordapi.response.embed.structure.Field;
import dev.sbs.discordapi.response.embed.structure.Footer;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.discordapi.response.page.handler.item.CollectionItemHandler;
import dev.sbs.discordapi.response.page.item.field.FieldItem;
import dev.sbs.simplifiedbot.util.SqlSlashCommand;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Instant;

@CommandId("1b072a71-2045-457a-a9f7-354b936567cb")
public class AboutCommand extends SqlSlashCommand {

    protected AboutCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) {
        return commandContext.reply(
            Response.builder()
                .replyMention()
                .isInteractable()
                .withTimeToLive(60)
                .withPages(
                    Page.builder()
                        .withOption(
                            SelectMenu.Option.builder()
                                .withValue("about")
                                .withLabel("About")
                                .withDescription("General information about the bot.")
                                .build()
                        )
                        .withEmbeds(
                            Embed.builder()
                                .withAuthor(
                                    Author.builder()
                                        .withName("Bot Information")
                                        .withIconUrl(getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                        .build()
                                )
                                .withTitle("About :: General")
                                .withColor(Color.DARK_GRAY)
                                .withDescription(
                                    "The official SkyBlock Simplified bot. Optimize your gear with `/optimizer`, " +
                                        "lookup guilds with `/guild`, lookup data on players such as general details, " +
                                        "net worth, skills, dungeons, slayers and more with `/player`. Look for Hypixel " +
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
                                                SimplifiedApi.getRepositoryOf(UserModel.class)
                                                    .matchAll(UserModel::isDeveloper)
                                                    .stream()
                                                    .map(userModel -> String.format("<@%s>", userModel.getDiscordIds().get(0)))
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
                                    "Premium",
                                    """
                                        Paying for Patreon comes with perks available on the SkyBlock Simplified Discord, Bot, Mod, Website and API.
                                        The full details of available perks can be seen on the Patreon page in the menu below.
                                        Sign up here: N/A"""
                                )
                                .withFooter(
                                    Footer.builder()
                                        .withTimestamp(Instant.now())
                                        .build()
                                )
                                .build()
                        )
                        .withPages(
                            Page.builder()
                                .withOption(
                                    SelectMenu.Option.builder()
                                        .withValue("donors")
                                        .withLabel("Donors")
                                        .withDescription("All legacy donors of the SkyBlock Simplified discord.")
                                        .build()
                                )
                                .withEmbeds(
                                    Embed.builder()
                                        .withAuthor(
                                            Author.builder()
                                                .withName("Bot Information")
                                                .withIconUrl(getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                                .build()
                                        )
                                        .withTitle("About :: Donors")
                                        .withColor(Color.DARK_GRAY)
                                        .withDescription("A big thank you goes out to those who donated early on in the development of the SkyBlock Simplified discord and bot!")
                                        .withFooter(
                                            Footer.builder()
                                                .withTimestamp(Instant.now())
                                                .build()
                                        )
                                        .build()
                                )
                                .withItemHandler(
                                    CollectionItemHandler.builder(SbsLegacyDonorModel.class)
                                        .withItems(SimplifiedApi.getRepositoryOf(SbsLegacyDonorModel.class).findAll())
                                        .withTransformer((legacyDonorModel, index, size) -> FieldItem.builder()
                                            .withLabel(String.format("<@%s>", legacyDonorModel.getDiscordId()))
                                            .withData(String.format("$%.2f", legacyDonorModel.getAmount()))
                                            .build()
                                        )
                                        .build()
                                )
                                .build(),
                            Page.builder()
                                .withOption(
                                    SelectMenu.Option.builder()
                                        .withValue("premium")
                                        .withLabel("Premium")
                                        .withDescription("Details and list of perks available for Patreon users.")
                                        .build()
                                )
                                .withEmbeds(
                                    Embed.builder()
                                        .withAuthor(
                                            Author.builder()
                                                .withName("Bot Information")
                                                .withIconUrl(getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                                .build()
                                        )
                                        .withTitle("About :: Patreon")
                                        .withColor(Color.DARK_GRAY)
                                        .withDescription("Perks will be listed here as they become available. The Patreon system is not live yet.")
                                        .withFooter(
                                            Footer.builder()
                                                .withTimestamp(Instant.now())
                                                .build()
                                        )
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
