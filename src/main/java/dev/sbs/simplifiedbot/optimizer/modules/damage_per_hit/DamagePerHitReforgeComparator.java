package dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.api.stream.pair.Pair;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatModel;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeComparator;

import java.util.Map;

public class DamagePerHitReforgeComparator extends ReforgeComparator {

    static final ConcurrentMap<StatModel, Integer> importantStatWeights = Concurrent.newMap(
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "DAMAGE"), 1),
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "STRENGTH"), 5),
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "CRITICAL_DAMAGE"), 5)
    );

    @Override
    protected Map<StatModel, Integer> getImportantStatWeights() {
        return importantStatWeights;
    }

}
