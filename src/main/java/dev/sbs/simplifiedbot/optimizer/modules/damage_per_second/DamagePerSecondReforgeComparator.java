package dev.sbs.simplifiedbot.optimizer.modules.damage_per_second;

import dev.sbs.api.data.model.skyblock.stats.StatModel;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentMap;
import dev.sbs.api.util.mutable.pair.Pair;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeComparator;

import java.util.Map;

public final class DamagePerSecondReforgeComparator extends ReforgeComparator {

    static final ConcurrentMap<StatModel, Integer> importantStatWeights = Concurrent.newMap(
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "DAMAGE"), 1),
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "STRENGTH"), 5),
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "CRITICAL_DAMAGE"), 5),
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "CRIT_CHANCE"), 10),
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "ATTACK_SPEED"), 15),
        Pair.of(statRepository.findFirstOrNull(StatModel::getKey, "FEROCITY"), 15)
    );

    @Override
    protected Map<StatModel, Integer> getImportantStatWeights() {
        return importantStatWeights;
    }

}
