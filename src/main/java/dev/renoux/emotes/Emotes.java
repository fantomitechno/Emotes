package dev.renoux.emotes;

import dev.renoux.emotes.config.ServerConfig;
import dev.renoux.emotes.utils.EmoteProcessor;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.quiltmc.config.api.ConfigEnvironment;
import org.quiltmc.config.api.serializers.TomlSerializer;
import org.quiltmc.config.implementor_api.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class Emotes implements DedicatedServerModInitializer {
    public static ModMetadata metadata;
    public static Logger LOGGER;

    public static ServerConfig serverConfig;

    @Override
    public void onInitializeServer() {
        metadata = FabricLoader.getInstance().getModContainer("emotes").get().getMetadata();
        LOGGER = LoggerFactory.getLogger(metadata.getId());
        serverConfig = ConfigFactory.create(new ConfigEnvironment(FabricLoader.getInstance().getConfigDir(), TomlSerializer.INSTANCE), metadata.getId(), metadata.getId(), Paths.get(""), builder -> {}, ServerConfig.class, builder -> {});

        LOGGER.info("{} : LOADING", metadata.getName());

        try {
            Events.init(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        EmoteProcessor.init();

        LOGGER.info("{} : LOADED", metadata.getName());
    }
}
