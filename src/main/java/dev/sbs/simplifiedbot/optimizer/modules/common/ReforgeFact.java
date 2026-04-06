package dev.sbs.simplifiedbot.optimizer.modules.common;

import dev.simplified.collection.ConcurrentMap;
import dev.sbs.minecraftapi.persistence.model.Reforge;
import dev.sbs.minecraftapi.skyblock.common.Rarity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.util.Objects;
import java.util.UUID;

/**
 * A wrapper for {@link Reforge} at a specific {@link Rarity} so that a {@link PlanningId} can be assigned to each reforge
 */
@RequiredArgsConstructor
public final class ReforgeFact {

    @PlanningId
    @Getter private final UUID uniqueId = UUID.randomUUID();
    @Getter private final Reforge reforge;
    @Getter private final Rarity rarity;
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
        return Objects.equals(this.getReforge(), that.getReforge())
                && Objects.equals(this.getRarity(), that.getRarity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getReforge(), this.getRarity());
    }

    @Override
    public String toString() {
        return "ReforgeProblemFact(reforge=" + this.getReforge().getId() + ", rarity=" + this.getRarity().name() + ")";
    }

    public boolean effectivelyEquals(ReforgeFact that) {
        if (that == null) return false;
        return Objects.equals(this.getReforge(), that.getReforge()) && Objects.equals(this.getRarity(), that.getRarity());
    }

}
