package dev.sbs.simplifiedbot.optimizer.modules.damage_per_second;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.api.tuple.pair.Pair;
import dev.sbs.minecraftapi.persistence.model.Stat;
import dev.sbs.simplifiedbot.optimizer.modules.common.ReforgeComparator;

import java.util.Map;

public final class DamagePerSecondReforgeComparator extends ReforgeComparator {

    static final ConcurrentMap<Stat, Integer> importantStatWeights = Concurrent.newMap(
        Pair.of(statRepository.findFirstOrNull(Stat::getId, "DAMAGE"), 1),
        Pair.of(statRepository.findFirstOrNull(Stat::getId, "STRENGTH"), 5),
        Pair.of(statRepository.findFirstOrNull(Stat::getId, "CRIT_DAMAGE"), 5),
        Pair.of(statRepository.findFirstOrNull(Stat::getId, "CRIT_CHANCE"), 10),
        Pair.of(statRepository.findFirstOrNull(Stat::getId, "BONUS_ATTACK_SPEED"), 15),
        Pair.of(statRepository.findFirstOrNull(Stat::getId, "FEROCITY"), 15)
    );

    @Override
    protected Map<Stat, Integer> getImportantStatWeights() {
        return importantStatWeights;
    }

}
