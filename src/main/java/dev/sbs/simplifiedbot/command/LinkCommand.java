package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.impl.hypixel.request.HypixelRequest;
import dev.sbs.api.client.impl.hypixel.response.hypixel.HypixelPlayerResponse;
import dev.sbs.api.client.impl.hypixel.response.hypixel.implementation.HypixelPlayer;
import dev.sbs.api.client.impl.hypixel.response.hypixel.implementation.HypixelSocial;
import dev.sbs.api.client.impl.sbs.request.SbsRequest;
import dev.sbs.api.client.impl.sbs.response.MojangProfileResponse;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.data.model.discord.users.UserModel;
import dev.sbs.api.data.model.discord.users.UserSqlModel;
import dev.sbs.api.data.sql.SqlRepository;
import dev.sbs.api.util.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.CommandStructure;
import dev.sbs.discordapi.command.SlashCommand;
import dev.sbs.discordapi.command.exception.input.ExpectedInputException;
import dev.sbs.discordapi.command.parameter.Argument;
import dev.sbs.discordapi.command.parameter.Parameter;
import dev.sbs.discordapi.context.deferrable.command.SlashCommandContext;
import dev.sbs.discordapi.exception.DiscordUserException;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.embed.structure.Author;
import dev.sbs.discordapi.response.page.Page;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandStructure("48b8f351-4e74-4010-b1ef-9b3d18c9833a")
public class LinkCommand extends SlashCommand {

    protected LinkCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull SlashCommandContext commandContext) {
        String playerID = commandContext.getArgument("name").map(Argument::asString).orElseThrow(); // Will never throw
        SbsRequest mojangRequest = SimplifiedApi.getApiRequest(SbsRequest.class);
        MojangProfileResponse mojangProfileResponse = StringUtil.isUUID(playerID) ? mojangRequest.getProfileFromUniqueId(StringUtil.toUUID(playerID)) : mojangRequest.getProfileFromUsername(playerID);
        HypixelPlayerResponse hypixelPlayerResponse = SimplifiedApi.getApiRequest(HypixelRequest.class).getPlayer(mojangProfileResponse.getUniqueId());
        String interactDiscordTag = commandContext.getInteractUser().getTag();

        String hypixelDiscordTag = hypixelPlayerResponse.getPlayer()
            .map(HypixelPlayer::getSocialMedia)
            .map(socialMedia -> socialMedia.getLinks().get(HypixelSocial.Type.DISCORD))
            .orElse("");

        if (interactDiscordTag.equals(hypixelDiscordTag)) {
            UserModel userModel = SimplifiedApi.getRepositoryOf(UserModel.class).matchFirstOrNull(user ->
                user.getDiscordIds().contains(commandContext.getInteractUserId().asLong()) ||
                    user.getMojangUniqueIds().contains(mojangProfileResponse.getUniqueId())
            );

            String message = String.format("You have linked `%s` to your Discord account.", mojangProfileResponse.getUsername());

            if (userModel == null) {
                // Create New User
                UserSqlModel newUserModel = new UserSqlModel();
                newUserModel.getDiscordIds().add(commandContext.getInteractUserId().asLong());
                newUserModel.getMojangUniqueIds().add(mojangProfileResponse.getUniqueId());

                // Save New User
                ((SqlRepository<UserSqlModel>) SimplifiedApi.getRepositoryOf(UserSqlModel.class)).save(newUserModel);
            } else {
                boolean alreadyVerified = false;

                // Update Existing User
                if (!userModel.getDiscordIds().contains(commandContext.getInteractUserId().asLong())) {
                    userModel.getDiscordIds().add(commandContext.getInteractUserId().asLong());
                    message = String.format("You have linked your new Discord account to `%s`.", mojangProfileResponse.getUsername());
                } else if (!userModel.getMojangUniqueIds().contains(mojangProfileResponse.getUniqueId()))
                    userModel.getMojangUniqueIds().add(mojangProfileResponse.getUniqueId());
                else
                    alreadyVerified = true;

                // Update User
                if (!alreadyVerified)
                    ((SqlRepository<UserSqlModel>) SimplifiedApi.getRepositoryOf(UserSqlModel.class)).update((UserSqlModel) userModel);
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
                                            .withIconUrl(getEmoji("STATUS_INFO").map(Emoji::getUrl))
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
