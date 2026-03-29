package dev.sbs.simplifiedbot.optimizer.modules.common;

import dev.sbs.api.persistence.Repository;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.persistence.model.Stat;

import java.util.Comparator;
import java.util.Map;

public abstract class ReforgeComparator implements Comparator<ReforgeFact> {

    protected static final Repository<Stat> statRepository = MinecraftApi.getRepository(Stat.class);

    protected abstract Map<Stat, Integer> getImportantStatWeights();

    @Override
    public int compare(ReforgeFact o1, ReforgeFact o2) {
        double sum1 = this.getImportantStatWeights().entrySet()
                .stream()
                .mapToDouble(entry -> o1.getEffect(entry.getKey().getId()) * entry.getValue())
                .sum();

        double sum2 = this.getImportantStatWeights().entrySet()
                .stream()
                .mapToDouble(entry -> o2.getEffect(entry.getKey().getId()) * entry.getValue())
                .sum();

        return Double.compare(sum1, sum2);
    }

}
