package dev.sbs.simplifiedbot.optimizer.modules.common;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.Repository;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatModel;

import java.util.Comparator;
import java.util.Map;

public abstract class ReforgeComparator implements Comparator<ReforgeFact> {

    protected static final Repository<StatModel> statRepository = SimplifiedApi.getRepositoryOf(StatModel.class);

    protected abstract Map<StatModel, Integer> getImportantStatWeights();

    @Override
    public int compare(ReforgeFact o1, ReforgeFact o2) {
        double sum1 = this.getImportantStatWeights().entrySet()
                .stream()
                .mapToDouble(entry -> o1.getEffect(entry.getKey().getKey()) * entry.getValue())
                .sum();

        double sum2 = this.getImportantStatWeights().entrySet()
                .stream()
                .mapToDouble(entry -> o2.getEffect(entry.getKey().getKey()) * entry.getValue())
                .sum();

        return Double.compare(sum1, sum2);
    }

}
