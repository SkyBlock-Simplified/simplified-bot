package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

public interface AppApplicationRequirement extends Model {

    String getKey();

    String getName();

    String getDescription();

    Integer getOrdinal();

}
