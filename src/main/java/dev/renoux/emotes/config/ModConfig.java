package dev.renoux.emotes.config;

import static dev.renoux.emotes.Emotes.MODID;
import net.fabricmc.loader.api.FabricLoader;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.ConfigEnvironment;
import org.quiltmc.config.api.serializers.TomlSerializer;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;
import org.quiltmc.config.implementor_api.ConfigFactory;

import java.nio.file.Paths;

public class ModConfig extends ReflectiveConfig {
    public static final ModConfig INSTANCE = ConfigFactory.create(new ConfigEnvironment(FabricLoader.getInstance().getConfigDir(), TomlSerializer.INSTANCE), MODID, MODID, Paths.get(""), builder -> {}, ModConfig.class, builder -> {});

    public final TrackedValue<ValueList<String>> emotes = this.list("", "kappa:KappaTest", "kappa:KappaTest");
    public final TrackedValue<Boolean> show_suggestion = this.value(false);
    
}
