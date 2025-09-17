package dev.sbs.simplifiedbot.model;

import dev.sbs.api.data.Model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AppUser extends Model {

    List<Long> getDiscordIds();

    List<UUID> getMojangUniqueIds();

    Map<Long, String> getNotes();

    List<Long> getGuildInteractionBlacklisted();

    boolean isDeveloper();

    boolean isDeveloperProtected();

    boolean isBotInteractionEnabled();

}
