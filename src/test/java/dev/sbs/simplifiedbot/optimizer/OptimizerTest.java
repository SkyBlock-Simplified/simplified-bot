package dev.sbs.simplifiedbot.optimizer;

import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.persistence.model.Item;
import dev.sbs.minecraftapi.persistence.model.Reforge;
import dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit.DamagePerHitSolution;
import dev.sbs.simplifiedbot.optimizer.modules.damage_per_second.DamagePerSecondSolution;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerResponse;
import dev.sbs.simplifiedbot.persistence.model.OptimizerMobType;
import dev.simplified.persistence.JpaConfig;
import dev.simplified.persistence.JpaSession;
import dev.simplified.util.StringUtil;
import org.junit.jupiter.api.Test;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

public class OptimizerTest {

    private static final PlannerBenchmarkFactory damagePerHitBenchmarker = PlannerBenchmarkFactory.createFromXmlResource("optaplanner/damagePerHitBenchmark.xml");
    private static final PlannerBenchmarkFactory damagePerSecondBenchmarker = PlannerBenchmarkFactory.createFromXmlResource("optaplanner/damagePerSecondBenchmark.xml");
    
    private void initializeDatabase() {
        System.out.println("Database Starting... ");
        JpaSession session = MinecraftApi.getSessionManager().connect(JpaConfig.commonSql());
        System.out.println("Database initialized in " + session.getInitialization().getDurationMillis() + "ms");
        System.out.println("Database started in " + session.getRepositoryCache().getDurationMillis() + "ms");
    }

    private static final String profileName = "PINEAPPLE";
    private static final String playerName = "CraftedFury"; // CraftedFury TheNewJavaman AspectOfTheUwU
    private static final String mobType = "ENDERMAN";
    private static final String weapon = "DAEDALUS_AXE"; // HYPERION, ASPECT_OF_THE_DRAGON, MIDAS_SWORD, ASPECT_OF_THE_VOID, ATOMSPLIT_KATANA, DAEDALUS_AXE

    private OptimizerRequest buildRequest(OptimizerRequest.Type type) {
        return OptimizerRequest.of(playerName)
            .withAllowedReforges(MinecraftApi.getRepository(Reforge.class).findAll())
            .withIsland(0)
            .withMobType(MinecraftApi.getRepository(OptimizerMobType.class).findFirstOrNull(OptimizerMobType::getKey, mobType))
            .withWeapon(MinecraftApi.getRepository(Item.class).findFirstOrNull(Item::getId, weapon))
            .withType(type)
            .build();
    }

    @Test
    public void damagePerHit_ok() {
        this.initializeDatabase();
        OptimizerRequest request = this.buildRequest(OptimizerRequest.Type.DAMAGE_PER_HIT);
        OptimizerResponse response = Optimizer.solve(request);
        System.out.println("Final damage: " + response.getFinalDamage());
        System.out.println("Time spent: " + response.getDuration().toMillis() + "ms");
        response.getReforgeCount().forEach((reforge, value) -> System.out.println("[" + StringUtil.join(reforge.getReforge().getCategoryIds(), ", ") + "] " + reforge.getRarity().getName() + " " + reforge.getReforge().getName() + ": " + value));
    }

    @Test
    public void damagePerSecond_ok() {
        this.initializeDatabase();
        OptimizerRequest request = this.buildRequest(OptimizerRequest.Type.DAMAGE_PER_SECOND);
        OptimizerResponse response = Optimizer.solveDamagePerSecond(request);
        System.out.println("Final damage: " + response.getFinalDamage());
        System.out.println("Time spent: " + response.getDuration().toMillis() + "ms");
        response.getReforgeCount().forEach((reforge, value) -> System.out.println("[" + StringUtil.join(reforge.getReforge().getCategoryIds(), ", ") + "] " + reforge.getRarity().getName() + " " + reforge.getReforge().getName() + ": " + value));
    }

    @Test
    public void optaplanner_damagePerHit_benchmark() {
        this.initializeDatabase();
        OptimizerRequest request = this.buildRequest(OptimizerRequest.Type.DAMAGE_PER_HIT);
        DamagePerHitSolution initialSolution = new DamagePerHitSolution(request);
        PlannerBenchmark benchmark = damagePerHitBenchmarker.buildPlannerBenchmark(initialSolution);
        benchmark.benchmarkAndShowReportInBrowser();
    }

    @Test
    public void optaplanner_damagePerSecond_benchmark() {
        this.initializeDatabase();
        OptimizerRequest request = this.buildRequest(OptimizerRequest.Type.DAMAGE_PER_SECOND);
        DamagePerSecondSolution initialSolution = new DamagePerSecondSolution(request);
        PlannerBenchmark benchmark = damagePerSecondBenchmarker.buildPlannerBenchmark(initialSolution);
        benchmark.benchmarkAndShowReportInBrowser();
    }
}
