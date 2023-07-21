package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.util.helper.ResourceUtil;
import dev.sbs.api.util.helper.StringUtil;
import dev.sbs.discordapi.util.DiscordConfig;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public class SimplifiedConfig extends DiscordConfig {

    @Getter private Optional<UUID> hypixelApiKey = ResourceUtil.getEnv("HYPIXEL_API_KEY").map(StringUtil::toUUID);

    public SimplifiedConfig(@NotNull String fileName, @NotNull String... header) {
        this(SimplifiedApi.getCurrentDirectory(), fileName, header);
        this.hypixelApiKey.ifPresent(value -> SimplifiedApi.getKeyManager().add("HYPIXEL_API_KEY", value));
    }

    public SimplifiedConfig(@NotNull File configDir, @NotNull String fileName, @NotNull String... header) {
        super(configDir, fileName, header);
    }

    public void setHypixelApiKey(@Nullable String hypixelApiKey) {
        if (StringUtil.isNotEmpty(hypixelApiKey))
            this.setHypixelApiKey(StringUtil.toUUID(hypixelApiKey));
        else
            this.setHypixelApiKey(Optional.empty());
    }

    public void setHypixelApiKey(@Nullable UUID hypixelApiKey) {
        this.setHypixelApiKey(Optional.ofNullable(hypixelApiKey));
    }

    public void setHypixelApiKey(@NotNull Optional<UUID> hypixelApiKey) {
        this.hypixelApiKey = hypixelApiKey;
    }

}
