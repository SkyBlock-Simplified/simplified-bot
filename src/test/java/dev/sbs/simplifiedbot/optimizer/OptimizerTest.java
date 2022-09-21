package dev.sbs.simplifiedbot.optimizer;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.data.model.discord.optimizer_mob_types.OptimizerMobTypeModel;
import dev.sbs.api.data.model.skyblock.items.ItemModel;
import dev.sbs.api.data.model.skyblock.profiles.ProfileModel;
import dev.sbs.api.data.model.skyblock.reforge_stats.ReforgeStatModel;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.util.DiscordConfig;
import dev.sbs.simplifiedbot.optimizer.modules.damage_per_hit.DamagePerHitSolution;
import dev.sbs.simplifiedbot.optimizer.modules.damage_per_second.DamagePerSecondSolution;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerRequest;
import dev.sbs.simplifiedbot.optimizer.util.OptimizerResponse;
import org.junit.jupiter.api.Test;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import java.io.File;

public class OptimizerTest {

    private static final PlannerBenchmarkFactory damagePerHitBenchmarker = PlannerBenchmarkFactory.createFromXmlResource("optaplanner/damagePerHitBenchmark.xml");
    private static final PlannerBenchmarkFactory damagePerSecondBenchmarker = PlannerBenchmarkFactory.createFromXmlResource("optaplanner/damagePerSecondBenchmark.xml");
    private static final DiscordConfig config;

    static {
        try {
            File currentDir = new File(SimplifiedApi.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            config = new DiscordConfig(currentDir.getParentFile(), "simplified-discord");
        } catch (Exception exception) {
            throw new IllegalArgumentException("Unable to retrieve current directory!", exception); // Should never get here
        }
    }
    
    private void initializeDatabase() {
        System.out.println("Database Starting... ");
        SimplifiedApi.connectDatabase(config);
        System.out.println("Database initialized in " + SimplifiedApi.getSqlSession().getInitializationTime() + "ms");
        System.out.println("Database started in " + SimplifiedApi.getSqlSession().getStartupTime() + "ms");
    }

    private static final String profileName = "PINEAPPLE";
    private static final String playerName = "CraftedFury"; // CraftedFury TheNewJavaman AspectOfTheUwU
    private static final String mobType = "ENDERMAN";
    private static final String weapon = "DAEDALUS_AXE"; // HYPERION, ASPECT_OF_THE_DRAGON, MIDAS_SWORD, ASPECT_OF_THE_VOID, ATOMSPLIT_KATANA, DAEDALUS_AXE

    private OptimizerRequest buildRequest(OptimizerRequest.Type type) {
        return OptimizerRequest.of(playerName)
            .withAllowedReforges(SimplifiedApi.getRepositoryOf(ReforgeStatModel.class).findAll())
            .withIsland(SimplifiedApi.getRepositoryOf(ProfileModel.class).findFirstOrNull(ProfileModel::getKey, profileName))
            .withMobType(SimplifiedApi.getRepositoryOf(OptimizerMobTypeModel.class).findFirstOrNull(OptimizerMobTypeModel::getKey, mobType))
            .withWeapon(SimplifiedApi.getRepositoryOf(ItemModel.class).findFirstOrNull(ItemModel::getItemId, weapon))
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
        response.getReforgeCount().forEach((reforge, value) -> System.out.println("[" + StringUtil.join(reforge.getReforge().getItemTypes(), ", ") + "] " + reforge.getRarity().getName() + " " + reforge.getReforge().getName() + ": " + value));
    }

    @Test
    public void damagePerSecond_ok() {
        this.initializeDatabase();
        OptimizerRequest request = this.buildRequest(OptimizerRequest.Type.DAMAGE_PER_SECOND);
        OptimizerResponse response = Optimizer.solveDamagePerSecond(request);
        System.out.println("Final damage: " + response.getFinalDamage());
        System.out.println("Time spent: " + response.getDuration().toMillis() + "ms");
        response.getReforgeCount().forEach((reforge, value) -> System.out.println("[" + StringUtil.join(reforge.getReforge().getItemTypes(), ", ") + "] " + reforge.getRarity().getName() + " " + reforge.getReforge().getName() + ": " + value));
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
