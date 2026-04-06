package dev.sbs.simplifiedbot.data;

import dev.sbs.api.SimplifiedApi;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.query.SearchFunction;
import dev.simplified.persistence.JpaConfig;
import dev.simplified.persistence.JpaModel;
import dev.simplified.persistence.JpaRepository;
import dev.simplified.persistence.JpaSession;
import dev.simplified.scheduler.Scheduler;
import dev.simplified.collection.tuple.pair.Pair;
import dev.simplified.util.mutable.MutableInt;
import dev.sbs.minecraftapi.persistence.model.Item;
import dev.sbs.minecraftapi.persistence.model.Stat;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Table;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SqlRepositoryTest {

    @Test
    public void checkRepositories_ok() {
        try {
            System.out.println("Database Starting... ");
            JpaSession session = SimplifiedApi.getSessionManager().connect(JpaConfig.commonSql());
            System.out.println("Database initialized in " + session.getInitialization().getDurationMillis() + "ms");
            System.out.println("Database started in " + session.getRepositoryCache().getDurationMillis() + "ms");

            final JpaRepository<Stat> statRepository = (JpaRepository<Stat>) SimplifiedApi.getRepository(Stat.class);
            long time_loopStart = System.currentTimeMillis();
            long time_loopEnd = time_loopStart + (3 * 60 * 1000);
            final MutableInt repeat = new MutableInt();

            Scheduler scheduler = new Scheduler();
            scheduler.scheduleAsync(() -> {
                Stat statSqlModel = statRepository.findFirstOrNull(Stat::getId, "MAGIC_FIND");
                statSqlModel.setName("Magic Find");
                statSqlModel.update();
            }, 0L, 35L, TimeUnit.SECONDS);

            while (System.currentTimeMillis() < time_loopEnd) {
                long beforeFind = System.currentTimeMillis();
                Stat statSqlModel = statRepository.findFirstOrNull(Stat::getId, "MAGIC_FIND");
                long afterFind = System.currentTimeMillis();
                long diff = afterFind - beforeFind;
                System.out.printf("Getting %s:%s (%s) took: %s%n", statSqlModel.getId(), statSqlModel.getName(), repeat.getAndIncrement(), diff);

                Scheduler.sleep(3 * 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void searchTest_ok() {
        System.out.println("Database Starting... ");
        JpaSession session = SimplifiedApi.getSessionManager().connect(JpaConfig.commonSql());
        System.out.println("Database initialized in " + session.getInitialization().getDurationMillis() + "ms");
        System.out.println("Database started in " + session.getRepositoryCache().getDurationMillis() + "ms");

        runTest(Stat.class, Stat::getId, "MAGIC_FIND");
        runTest(Item.class, Item::getId, "WITHER_RELIC");
    }

    private static <T extends JpaModel, S> void runTest(@NotNull Class<T> model, @NotNull SearchFunction<T, S> function, @NotNull S value) {
        long before1 = ctm();
        ConcurrentList<T> result1 = SimplifiedApi.getRepository(model).findAll();
        System.out.printf("%s: 1.1 took %s%n", model.getSimpleName(), (ctm() - before1));

        long before2 = ctm();
        T result2 = result1.findFirstOrNull(function, value);
        System.out.printf("%s: 2.1 took %s%n", model.getSimpleName(), (ctm() - before2));

        long before4 = ctm();
        T model4 = SimplifiedApi.getRepository(model).findFirstOrNull(function, value);
        System.out.printf("%s: 4.1 took %s%n", model.getSimpleName(), (ctm() - before4));

        long before42 = ctm();
        T model42 = SimplifiedApi.getRepository(model).findAll().findFirstOrNull(function, value);
        System.out.printf("%s: 4.2 took %s%n", model.getSimpleName(), (ctm() - before42));
    }

    public static long ctm() {
        return System.currentTimeMillis();
    }

    @Test
    @SuppressWarnings("all")
    public void dumpDatabaseToJson_ok() {
        File currentDir = SimplifiedApi.getCurrentDirectory();
        File dbDir = new File(currentDir, "build/db");

        if (!dbDir.exists())
            dbDir.mkdirs();

        System.out.println("Connecting to database...");
        JpaSession session = SimplifiedApi.getSessionManager().connect(JpaConfig.commonSql());

        session.getModels()
            .stream()
            .filter(modelClass -> modelClass.isAnnotationPresent(Table.class))
            .map(modelClass -> Pair.of(
                modelClass,
                modelClass.getAnnotation(Table.class).name()
            ))
            .forEach(entry -> {
                String tableName = entry.getValue();
                System.out.println("Saving " + tableName + "...");

                try (FileWriter fileWriter = new FileWriter(dbDir.getAbsolutePath() + "/" + tableName + ".json")) {
                    fileWriter.write(SimplifiedApi.getGson().toJson(SimplifiedApi.getRepository(entry.getKey()).findAll()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

}