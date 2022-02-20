package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.SimplifiedException;
import dev.sbs.api.client.hypixel.implementation.HypixelPlayerData;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelPlayerResponse;
import dev.sbs.api.client.mojang.implementation.MojangData;
import dev.sbs.api.client.mojang.response.MojangProfileResponse;
import dev.sbs.api.data.model.discord.users.UserModel;
import dev.sbs.api.data.model.discord.users.UserSqlModel;
import dev.sbs.api.data.model.discord.users.UserSqlRepository;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.api.util.tuple.Pair;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.Argument;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.command.exception.user.UserInputException;
import dev.sbs.discordapi.command.exception.user.UserVerificationException;
import dev.sbs.discordapi.context.command.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.util.exception.DiscordException;
import discord4j.core.object.entity.User;
import org.jetbrains.annotations.NotNull;

@CommandInfo(
    id = "48b8f351-4e74-4010-b1ef-9b3d18c9833a",
    name = "verify"
    //description = "Link your Hypixel Account to your Discord Account."
)
public class VerifyCommand extends Command {

    protected VerifyCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected void process(CommandContext<?> commandContext) {
        String playerID = commandContext.getArgument("name").flatMap(Argument::getValue).orElseThrow(); // Will never throw
        MojangData mojangData = SimplifiedApi.getWebApi(MojangData.class);
        MojangProfileResponse mojangProfileResponse = StringUtil.isUUID(playerID) ? mojangData.getProfileFromUniqueId(StringUtil.toUUID(playerID)) : mojangData.getProfileFromUsername(playerID);
        HypixelPlayerResponse hypixelPlayerResponse = SimplifiedApi.getWebApi(HypixelPlayerData.class).getPlayer(mojangProfileResponse.getUniqueId());

        String interactDiscordTag = commandContext.getInteractUser()
            .map(User::getTag)
            .blockOptional()
            .orElseThrow(() -> SimplifiedException.of(DiscordException.class)
                .withMessage("Unable to identify discord user!")
                .build()
            );

        String hypixelDiscordTag = hypixelPlayerResponse.getPlayer()
            .getSocialMedia()
            .getLinks()
            .getOrDefault(HypixelPlayerResponse.SocialMedia.Service.DISCORD, "");

        if (interactDiscordTag.equals(hypixelDiscordTag)) {
            UserModel userModel = SimplifiedApi.getRepositoryOf(UserModel.class).matchFirstOrNull(user ->
                user.getDiscordIds().contains(commandContext.getInteractUserId().asLong()) ||
                    user.getMojangUniqueIds().contains(mojangProfileResponse.getUniqueId())
            );

            String message = FormatUtil.format("You have linked `{0}` to your Discord account.", mojangProfileResponse.getUsername());

            if (userModel == null) {
                // Create New User
                UserSqlModel newUserModel = new UserSqlModel();
                newUserModel.getDiscordIds().add(commandContext.getInteractUserId().asLong());
                newUserModel.getMojangUniqueIds().add(mojangProfileResponse.getUniqueId());

                // Save User
                ((UserSqlRepository) SimplifiedApi.getRepositoryOf(UserSqlModel.class)).save(newUserModel);
            } else {
                boolean alreadyVerified = false;

                // Update Existing User
                if (userModel.getDiscordIds().contains(commandContext.getInteractUserId().asLong()))
                    userModel.getMojangUniqueIds().add(mojangProfileResponse.getUniqueId());
                else if (userModel.getMojangUniqueIds().contains(mojangProfileResponse.getUniqueId())) {
                    userModel.getDiscordIds().add(commandContext.getInteractUserId().asLong());
                    message = FormatUtil.format("You have linked your new Discord account to `{0}`.", mojangProfileResponse.getUsername());
                } else
                    alreadyVerified = true;

                // Save User
                ((UserSqlRepository) SimplifiedApi.getRepositoryOf(UserSqlModel.class)).save((UserSqlModel) userModel);

                // TODO: Assign verified member role

                if (alreadyVerified) {
                    // TODO: Only throw error if they have the verified membe role
                    throw SimplifiedException.of(UserVerificationException.class)
                        .addData("MESSAGE", true)
                        .withMessage("Your Discord account is already linked to `{0}`!", mojangProfileResponse.getUsername())
                        .build();
                }
            }

            // TODO: Check User Reports
            // Don't forget to assign back to userModel

            commandContext.reply(
                Response.builder()
                    .withReference(commandContext)
                    .isInteractable(false)
                    .withEmbeds(
                        Embed.builder()
                            .withAuthor("Hypixel Verification", getEmoji("STATUS_INFO").map(Emoji::getUrl))
                            .withDescription(message)
                            .build()
                    )
                    .build()
            );
        } else {
            String emptyError = "Your Hypixel account has no associated Discord tag.";
            String invalidError = "Your Hypixel account's Discord tag does not match your Discord account.";

            SimplifiedException.ExceptionBuilder<UserInputException> userInputError = SimplifiedException.of(UserInputException.class)
                .withMessage(StringUtil.isEmpty(hypixelDiscordTag) ? emptyError : invalidError);

            if (StringUtil.isNotEmpty(hypixelDiscordTag)) {
                userInputError.withFields(
                    Pair.of(
                        "Expected",
                        FormatUtil.format("`{0}`", interactDiscordTag)
                    ),
                    Pair.of(
                        "Found",
                        FormatUtil.format("`{0}`", hypixelDiscordTag)
                    )
                );
            }

            throw userInputError.build();
        }
    }

    @Override
    public @NotNull ConcurrentUnmodifiableList<Parameter> getParameters() {
        return Concurrent.newUnmodifiableList(
            new Parameter(
                "name",
                "Minecraft Username or UUID",
                Parameter.Type.WORD,
                true,
                (argument, commandContext) -> StringUtil.isUUID(argument) || PlayerCommand.MOJANG_NAME.matcher(argument).matches()
            )
        );
    }


}
