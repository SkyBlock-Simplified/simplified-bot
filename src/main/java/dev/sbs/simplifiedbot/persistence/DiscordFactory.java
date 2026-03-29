package dev.sbs.simplifiedbot.persistence;

import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.persistence.JpaModel;
import dev.sbs.api.persistence.RepositoryFactory;
import dev.sbs.api.persistence.strategy.RefreshStrategy;
import dev.sbs.simplifiedbot.persistence.model.AppGuild;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Repository factory for Discord SQL-backed models scoped to the
 * {@link AppGuild} package.
 */
@Getter
public class DiscordFactory implements RepositoryFactory {

    private final @NotNull ConcurrentList<Class<JpaModel>> models = RepositoryFactory.resolveModels(AppGuild.class);
    private final @NotNull RefreshStrategy<?> defaultStrategy = RefreshStrategy.sql();

}