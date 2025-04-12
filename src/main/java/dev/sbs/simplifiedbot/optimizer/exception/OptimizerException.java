package dev.sbs.simplifiedbot.optimizer.exception;

import dev.sbs.discordapi.util.exception.DiscordUserException;
import dev.sbs.simplifiedbot.optimizer.Optimizer;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link OptimizerException OptimizerExceptions} are thrown
 * when the {@link Optimizer} is unable to perform a specific action.
 */
public class OptimizerException extends DiscordUserException {

    public OptimizerException(@NotNull Throwable cause) {
        super(cause);
    }

    public OptimizerException(@NotNull String message) {
        super(message);
    }

    public OptimizerException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }

    public OptimizerException(@NotNull @PrintFormat String message, @Nullable Object... args) {
        super(String.format(message, args));
    }

}