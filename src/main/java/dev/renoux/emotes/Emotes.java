package dev.renoux.emotes;

import dev.renoux.emotes.config.ServerConfig;
import dev.renoux.emotes.utils.EmoteProcessor;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.quiltmc.config.api.ConfigEnvironment;
import org.quiltmc.config.api.serializers.TomlSerializer;
import org.quiltmc.config.implementor_api.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class Emotes implements DedicatedServerModInitializer {
    public static final String MODID = "emotes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final ServerConfig serverConfig = ConfigFactory.create(new ConfigEnvironment(FabricLoader.getInstance().getConfigDir(), TomlSerializer.INSTANCE), MODID, MODID, Paths.get(""), builder -> {}, ServerConfig.class, builder -> {});

    @Override
    public void onInitializeServer() {
        LOGGER.info("Emotes : LOADING");

        Events.init(false);

        EmoteProcessor.init();

        LOGGER.info("Emotes : LOADED");
    }
}
