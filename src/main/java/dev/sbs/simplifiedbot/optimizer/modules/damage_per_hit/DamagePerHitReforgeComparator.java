package dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit;

import dev.sbs.api.data.model.skyblock.stats.StatModel;
import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentMap;
import dev.sbs.api.util.tuple.Pair;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeComparator;

import java.util.Map;

public class DamagePerHitReforgeComparator extends ReforgeComparator {

    static final ConcurrentMap<StatModel, Integer> importantStatWeights = Concurrent.newMap(
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "STRENGTH"), 1),
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "CRITICAL_DAMAGE"), 1)
    );

    @Override
    protected Map<StatModel, Integer> getImportantStatWeights() {
        return importantStatWeights;
    }

}
