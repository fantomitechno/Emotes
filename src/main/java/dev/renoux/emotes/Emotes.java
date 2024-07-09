package dev.renoux.emotes;

import dev.renoux.emotes.utils.EmoteProcessor;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Emotes implements DedicatedServerModInitializer {
    public static final String MODID = "emotes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitializeServer() {
        LOGGER.info("Emotes : LOADING");

        Events.init(false);

        EmoteProcessor.init();

        LOGGER.info("Emotes : LOADED");
    }
}
