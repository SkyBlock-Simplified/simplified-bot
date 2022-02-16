package dev.sbs.simplifiedbot.optimizer.util;

import dev.sbs.api.util.builder.Builder;
import dev.sbs.simplifiedbot.optimizer.modules.common.Calculator;
import dev.sbs.simplifiedbot.optimizer.modules.common.ItemEntity;
import dev.sbs.simplifiedbot.optimizer.modules.common.Solution;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchType;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.util.UUID;

public final class OptimizerSolver {

    @Getter private final Class<? extends ItemEntity> itemEntityClass;
    @Getter private final Class<? extends Solution<? extends ItemEntity>> solutionClass;
    @Getter private final Class<? extends Calculator<? extends ItemEntity, ? extends Solution<? extends ItemEntity>>> calculatorClass;
    @Getter private final String moveThreadCount;
    @Getter private final long terminateAfterSecondsUnimproved;
    @Getter private final long terminateAfterSecondsSpent;
    @Getter private final EnvironmentMode environmentMode;
    @Getter private final ConstructionHeuristicType heuristicType;
    @Getter private final LocalSearchType searchType;

    private OptimizerSolver(SolverBuilder solverBuilder) {
        this.itemEntityClass = solverBuilder.itemEntityClass;
        this.solutionClass = solverBuilder.solutionClass;
        this.calculatorClass = solverBuilder.calculatorClass;
        this.moveThreadCount = solverBuilder.moveThreadCount;
        this.terminateAfterSecondsUnimproved = solverBuilder.terminateAfterSecondsUnimproved;
        this.terminateAfterSecondsSpent = solverBuilder.terminateAfterSecondsSpent;
        this.environmentMode = solverBuilder.environmentMode;
        this.heuristicType = solverBuilder.heuristicType;
        this.searchType = solverBuilder.searchType;
    }

    public static <I extends ItemEntity, S extends Solution<I>, C extends Calculator<I, S>> SolverBuilder builder(Class<I> itemEntityClass, Class<S> solutionClass, Class<C> calculatorClass) {
        return new SolverBuilder(itemEntityClass, solutionClass, calculatorClass);
    }

    public SolverConfig getConfig() {
        return new SolverConfig()
            .withMoveThreadCount(this.getMoveThreadCount())
            .withSolutionClass(this.getSolutionClass())
            .withEntityClasses(this.getItemEntityClass())
            .withEasyScoreCalculatorClass(this.getCalculatorClass())
            .withTerminationConfig(
                new TerminationConfig()
                    .withUnimprovedSecondsSpentLimit(this.getTerminateAfterSecondsUnimproved())
                    .withSecondsSpentLimit(this.getTerminateAfterSecondsSpent())
            )
            .withEnvironmentMode(this.getEnvironmentMode())
            .withPhases(
                new ConstructionHeuristicPhaseConfig().withConstructionHeuristicType(this.getHeuristicType()),
                new LocalSearchPhaseConfig().withLocalSearchType(this.getSearchType())
            );
    }

    public <I extends ItemEntity, S extends Solution<I>> SolverManager<S, UUID> getManager() {
        return SolverManager.create(this.getConfig());
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SolverBuilder implements Builder<OptimizerSolver> {

        private final Class<? extends ItemEntity> itemEntityClass;
        private final Class<? extends Solution<? extends ItemEntity>> solutionClass;
        private final Class<? extends Calculator<? extends ItemEntity, ? extends Solution<? extends ItemEntity>>> calculatorClass;
        private String moveThreadCount = "AUTO";
        private long terminateAfterSecondsUnimproved = 5L;
        private long terminateAfterSecondsSpent = 10L;
        private EnvironmentMode environmentMode = EnvironmentMode.REPRODUCIBLE;
        private ConstructionHeuristicType heuristicType = ConstructionHeuristicType.STRONGEST_FIT;
        private LocalSearchType searchType = LocalSearchType.TABU_SEARCH;

        public SolverBuilder withEnvironmentMode(EnvironmentMode environmentMode) {
            this.environmentMode = environmentMode;
            return this;
        }

        public SolverBuilder withHeuristicType(ConstructionHeuristicType heuristicType) {
            this.heuristicType = heuristicType;
            return this;
        }

        public SolverBuilder withMoveThreadCount(int threadCount) {
            this.moveThreadCount = String.valueOf(threadCount);
            return this;
        }

        public SolverBuilder withMoveThreadCountAuto() {
            this.moveThreadCount = "AUTO";
            return this;
        }

        public SolverBuilder withSearchType(LocalSearchType searchType) {
            this.searchType = searchType;
            return this;
        }

        public SolverBuilder withTerminationAfterSecondsSpent(long seconds) {
            this.terminateAfterSecondsSpent = seconds;
            return this;
        }

        public SolverBuilder withTerminationAfterSecondsUnimproved(long seconds) {
            this.terminateAfterSecondsUnimproved = seconds;
            return this;
        }

        @Override
        public OptimizerSolver build() {
            return new OptimizerSolver(this);
        }

    }

}
