package dev.sbs.simplifiedbot.command;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.implementation.HypixelPlayerData;
import dev.sbs.api.client.hypixel.response.hypixel.HypixelPlayerResponse;
import dev.sbs.api.client.sbs.implementation.MojangData;
import dev.sbs.api.client.sbs.response.MojangProfileResponse;
import dev.sbs.api.data.model.discord.users.UserModel;
import dev.sbs.api.data.model.discord.users.UserSqlModel;
import dev.sbs.api.data.model.discord.users.UserSqlRepository;
import dev.sbs.api.util.SimplifiedException;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.unmodifiable.ConcurrentUnmodifiableList;
import dev.sbs.api.util.data.tuple.Pair;
import dev.sbs.api.util.helper.FormatUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.DiscordBot;
import dev.sbs.discordapi.command.Command;
import dev.sbs.discordapi.command.data.Argument;
import dev.sbs.discordapi.command.data.CommandInfo;
import dev.sbs.discordapi.command.data.Parameter;
import dev.sbs.discordapi.command.exception.user.UserInputException;
import dev.sbs.discordapi.command.exception.user.UserVerificationException;
import dev.sbs.discordapi.context.CommandContext;
import dev.sbs.discordapi.response.Emoji;
import dev.sbs.discordapi.response.Response;
import dev.sbs.discordapi.response.embed.Embed;
import dev.sbs.discordapi.response.page.Page;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@CommandInfo(
    id = "48b8f351-4e74-4010-b1ef-9b3d18c9833a",
    name = "verify"
)
public class VerifyCommand extends Command {

    protected VerifyCommand(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    protected @NotNull Mono<Void> process(@NotNull CommandContext<?> commandContext) {
        String playerID = commandContext.getArgument("name").flatMap(Argument::getValue).orElseThrow(); // Will never throw
        MojangData mojangData = SimplifiedApi.getWebApi(MojangData.class);
        MojangProfileResponse mojangProfileResponse = StringUtil.isUUID(playerID) ? mojangData.getProfileFromUniqueId(StringUtil.toUUID(playerID)) : mojangData.getProfileFromUsername(playerID);
        HypixelPlayerResponse hypixelPlayerResponse = SimplifiedApi.getWebApi(HypixelPlayerData.class).getPlayer(mojangProfileResponse.getUniqueId());
        String interactDiscordTag = commandContext.getInteractUser().getTag();

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

                // Save New User
                ((UserSqlRepository) SimplifiedApi.getRepositoryOf(UserSqlModel.class)).save(newUserModel);
            } else {
                boolean alreadyVerified = false;

                // Update Existing User
                if (!userModel.getDiscordIds().contains(commandContext.getInteractUserId().asLong())) {
                    userModel.getDiscordIds().add(commandContext.getInteractUserId().asLong());
                    message = FormatUtil.format("You have linked your new Discord account to `{0}`.", mojangProfileResponse.getUsername());
                } else if (!userModel.getMojangUniqueIds().contains(mojangProfileResponse.getUniqueId()))
                    userModel.getMojangUniqueIds().add(mojangProfileResponse.getUniqueId());
                else
                    alreadyVerified = true;

                // Update User
                if (!alreadyVerified)
                    ((UserSqlRepository) SimplifiedApi.getRepositoryOf(UserSqlModel.class)).update((UserSqlModel) userModel);
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
                    throw SimplifiedException.of(UserVerificationException.class)
                        .addData("MESSAGE", true)
                        .withMessage("Your Discord account is already linked to `{0}`!", mojangProfileResponse.getUsername())
                        .build();
                }
            }

            return commandContext.reply(
                Response.builder()
                    .withReference(commandContext)
                    .isInteractable(false)
                    .withPages(
                        Page.builder()
                            .withEmbeds(
                                Embed.builder()
                                    .withAuthor("Hypixel Verification", getEmoji("STATUS_INFO").map(Emoji::getUrl))
                                    .withDescription(message)
                                    .build()
                            )
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
            Parameter.builder("name", "Minecraft Username or UUID", Parameter.Type.WORD)
                .withValidator((argument, commandContext) -> StringUtil.isUUID(argument) || PlayerCommand.MOJANG_NAME.matcher(argument).matches())
                .isRequired()
                .build()
        );
    }


}
