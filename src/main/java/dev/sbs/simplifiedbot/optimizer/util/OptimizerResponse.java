package dev.sbs.simplifiedbot.optimizer.util;

import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.api.data.model.skyblock.reforge_data.reforge_stats.ReforgeStatModel;
import dev.sbs.simplifiedbot.optimizer.modules.common.Solution;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@AllArgsConstructor
public final class OptimizerResponse {

    @Getter private final Solution<?> solution;
    @Getter private final ConcurrentMap<ReforgeStatModel, Integer> reforgeCount;
    @Getter private final double finalDamage;
    @Getter private final UUID problemId;
    @Getter private final Duration duration;

    public BigDecimal getScore() {
        return this.getSolution().getScore().score();
    }

}
