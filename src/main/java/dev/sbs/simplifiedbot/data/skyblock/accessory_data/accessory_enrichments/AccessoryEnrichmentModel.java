package dev.sbs.simplifiedbot.data.skyblock.accessory_data.accessory_enrichments;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.stats.StatModel;

public interface AccessoryEnrichmentModel extends Model {

    StatModel getStat();

    Double getValue();

    default String getName() {
        return String.format("%s Enrichment", this.getStat().getName());
    }

}
