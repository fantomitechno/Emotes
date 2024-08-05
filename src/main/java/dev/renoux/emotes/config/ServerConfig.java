package dev.renoux.emotes.config;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.SerializedName;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;

public class ServerConfig extends ReflectiveConfig {
    public final TrackedValue<ValueList<String>> emotes = this.list("", "kappa:KappaTest");

    @Comment("This will block the users from using autocomplete to use emotes. If set to false, they will need to types their exact names")
    @SerializedName("show_suggestions")
    public final TrackedValue<Boolean> showSuggestions = this.value(true);
}
