package dev.sbs.simplifiedbot.data;

import dev.sbs.api.SimplifiedApi;
import dev.simplified.persistence.JpaConfig;
import dev.simplified.persistence.Repository;
import dev.simplified.persistence.exception.JpaException;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.persistence.model.ItemCategory;
import dev.sbs.minecraftapi.persistence.model.Reforge;
import dev.sbs.minecraftapi.skyblock.common.Rarity;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ReforgeRepositoryTest {

    private static final Repository<ItemCategory> itemCategoryRepository;
    private static final Repository<Reforge> reforgeRepository;

    static {
        SimplifiedApi.getSessionManager().connect(JpaConfig.commonSql());
        itemCategoryRepository = MinecraftApi.getRepository(ItemCategory.class);
        reforgeRepository = MinecraftApi.getRepository(Reforge.class);
    }

    @Test
    public void findAll_ok() {
        List<Reforge> reforges = reforgeRepository.findAll();
        MatcherAssert.assertThat(reforges.size(), Matchers.greaterThan(0));
    }

    @Test
    public void getCachedList_ok() throws JpaException {
        ItemCategory sword = itemCategoryRepository.findFirstOrNull(
                ItemCategory::getId, "SWORD"
        );
        Rarity legendary = Rarity.LEGENDARY;
        Reforge spicy = reforgeRepository.matchFirstOrNull(
            reforge -> reforge.getCategoryIds().contains(sword.getId()) &&
                reforge.getId().equals("SPICY")
        );
        // Reforge stats are now embedded in the Reforge model as Substitute entries
        MatcherAssert.assertThat(spicy, Matchers.notNullValue());
        MatcherAssert.assertThat(spicy.getStats(legendary).size(), Matchers.greaterThan(0));
    }

}
