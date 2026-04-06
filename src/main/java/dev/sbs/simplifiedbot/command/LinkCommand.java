package dev.sbs.simplifiedbot.command;

import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.DiscordCommand;
import dev.sbs.discordapi.command.Structure;
import dev.sbs.discordapi.command.exception.ExpectedInputException;
import dev.sbs.discordapi.command.parameter.Argument;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.command.SlashCommandContext;
import dev.sbs.discordapi.exception.DiscordUserException;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Author;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.page.Page;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.client.hypixel.HypixelClient;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelPlayer;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelPlayerResponse;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelSocial;
import dev.sbs.minecraftapi.client.mojang.response.MojangProfile;
import dev.sbs.minecraftapi.client.sbs.SbsClient;
import dev.sbs.minecraftapi.client.sbs.request.SbsEndpoint;
import dev.sbs.simplifiedbot.persistence.model.AppUser;
import dev.simplified.collection.Concurrent;
import dev.simplified.collection.unmodifiable.ConcurrentUnmodifiableList;
import dev.simplified.persistence.JpaRepository;
import dev.simplified.util.StringUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@Structure(
    name = "link",
    description = "Link your Minecraft account to your Discord account"
)
public class LinkCommand extends DiscordCommand<SlashCommandContext> {

    protected LinkCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) {
        String playerID = commandContext.getArgument("name").map(Argument::asString).orElseThrow(); // Will never throw
        SbsEndpoint mojangRequest = MinecraftApi.getClient(SbsClient.class).getEndpoint();
        MojangProfile mojangProfile = StringUtil.isUUID(playerID) ? mojangRequest.getProfileFromUniqueId(StringUtil.toUUID(playerID)) : mojangRequest.getProfileFromUsername(playerID);
        HypixelPlayerResponse hypixelPlayerResponse = MinecraftApi.getClient(HypixelClient.class).getEndpoint().getPlayer(mojangProfile.getUniqueId());
        String interactDiscordTag = commandContext.getInteractUser().getTag();

        String hypixelDiscordTag = hypixelPlayerResponse.getPlayer()
            .map(HypixelPlayer::getSocialMedia)
            .map(socialMedia -> socialMedia.getLinks().get(HypixelSocial.Type.DISCORD))
            .orElse("");

        if (interactDiscordTag.equals(hypixelDiscordTag)) {
            AppUser user = MinecraftApi.getRepository(AppUser.class).matchFirstOrNull(appUser -> appUser.getDiscordIds()
                .contains(commandContext.getInteractUserId().asLong()) || appUser.getMojangUniqueIds().contains(mojangProfile.getUniqueId())
            );

            String message = String.format("You have linked `%s` to your Discord account.", mojangProfile.getUsername());

            if (user == null) {
                // Create New User
                AppUser newUserModel = new AppUser();
                newUserModel.getDiscordIds().add(commandContext.getInteractUserId().asLong());
                newUserModel.getMojangUniqueIds().add(mojangProfile.getUniqueId());

                // Save New User
                ((JpaRepository<AppUser>) MinecraftApi.getRepository(AppUser.class)).save(newUserModel);
            } else {
                boolean alreadyVerified = false;

                // Update Existing User
                if (!user.getDiscordIds().contains(commandContext.getInteractUserId().asLong())) {
                    user.getDiscordIds().add(commandContext.getInteractUserId().asLong());
                    message = String.format("You have linked your new Discord account to `%s`.", mojangProfile.getUsername());
                } else if (!user.getMojangUniqueIds().contains(mojangProfile.getUniqueId()))
                    user.getMojangUniqueIds().add(mojangProfile.getUniqueId());
                else
                    alreadyVerified = true;

                // Update User
                if (!alreadyVerified)
                    ((JpaRepository<AppUser>) MinecraftApi.getRepository(AppUser.class)).update(user);
                else
                    message = "Your Discord account has been verified.";

                // TODO: Check User Reports
                // Don't forget to assign back to userModel

                // TODO: Assign verified member role
                Member guildMember = commandContext.getGuild()
                    .flatMap(guild -> guild.getMemberById(commandContext.getInteractUserId()))
                    .blockOptional()
                    .orElseThrow(); // Shouldn't reach here

                boolean hasVerifiedRole = guildMember.getRoleIds().contains(Snowflake.of(862138423175544862L));

                if (!hasVerifiedRole)
                    guildMember.addRole(Snowflake.of(862138423175544862L)).subscribe();
                else {
                    // TODO: Assign linked accounts anyway
                    /*throw SimplifiedException.of(UserVerificationException.class)
                        .addData("MESSAGE", true)
                        .withMessage("Your Discord account is already linked to `%s`!", mojangProfileResponse.getUsername())
                        .build();*/
                }
            }

            return commandContext.reply(
                Response.builder()
                    .withPages(
                        Page.builder()
                            .withEmbeds(
                                Embed.builder()
                                    .withAuthor(
                                        Author.builder()
                                            .withName("Hypixel Verification")
                                            .withIconUrl(this.getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                            .build()
                                    )
                                    .withDescription(message)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );
        } else {
            if (StringUtil.isNotEmpty(hypixelDiscordTag))
                throw new ExpectedInputException(interactDiscordTag, hypixelDiscordTag, "Your Hypixel account's Discord tag does not match your Discord account.");
            else
                throw new DiscordUserException("Your Hypixel account has no associated Discord tag.");
        }
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            Parameter.builder()
                .withName("name")
                .withDescription("Minecraft Username or UUID")
                .withType(Parameter.Type.WORD)
                .withValidator((argument, commandContext) -> StringUtil.isUUID(argument) || PlayerCommand.MOJANG_NAME.matcher(argument).matches())
                .isRequired()
                .build()
        );
    }


}
