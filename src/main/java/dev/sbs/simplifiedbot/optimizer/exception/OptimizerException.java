package dev.sbs.simplifiedbot.optimizer.exception;

import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.mutable.tuple.triple.Triple;
import dev.sbs.discordapi.util.exception.DiscordException;
import dev.sbs.simplifiedbot.optimizer.Optimizer;

/**
 * {@link OptimizerException OptimizerExceptions} are thrown
 * when the {@link Optimizer} is unable to perform a specific action.
 */
public final class OptimizerException extends DiscordException {

    private OptimizerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ConcurrentList<Triple<String, String, Boolean>> fields, ConcurrentMap<String, Object> data) {
        super(message, cause, enableSuppression, writableStackTrace, fields, data);
    }

}