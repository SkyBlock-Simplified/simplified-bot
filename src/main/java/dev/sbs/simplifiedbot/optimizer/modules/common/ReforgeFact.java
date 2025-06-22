package dev.sbs.simplifiedbot.optimizer.modules.common;

import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforge_stats.ReforgeStatModel;
import dev.sbs.simplifiedbot.data.skyblock.reforge_data.reforge_stats.ReforgeStatSqlModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.util.Objects;
import java.util.UUID;

/**
 * A wrapper for {@link ReforgeStatSqlModel} so that a {@link PlanningId} can be assigned to each reforge
 */
@RequiredArgsConstructor
public final class ReforgeFact {

    @PlanningId
    @Getter private final UUID uniqueId = UUID.randomUUID();
    @Getter private final ReforgeStatModel reforge;
    @Getter private final ConcurrentMap<String, Double> effects;

    public double getEffect(String key) {
        return this.getEffects().getOrDefault(key, 0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReforgeFact that = (ReforgeFact) o;
        return Objects.equals(this.getReforge().getReforge(), that.getReforge().getReforge())
                && Objects.equals(this.getReforge().getRarity(), that.getReforge().getRarity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getReforge());
    }

    @Override
    public String toString() {
        return "ReforgeProblemFact(reforge=" + this.getReforge().getReforge().getKey() + ", rarity=" + this.getReforge().getRarity().getKey() + ")";
    }

    public boolean effectivelyEquals(ReforgeFact that) {
        if (that == null) return false;
        return this.getReforge().equals(that.getReforge());
    }

}
