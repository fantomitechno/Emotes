package dev.renoux.survival1emotesclient.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static dev.renoux.survival1emotesclient.Survival1EmotesClient.MODID;

public class EmoteUtil {

    public static final ResourceLocation CUSTOM_IMAGE_FONT_IDENTIFIER = new ResourceLocation(MODID, "emote_font");

    // I've found this is a pretty good scale factor for 24x24px Twitch emotes.
    public static final float CUSTOM_IMAGE_SCALE_FACTOR = 0.08f;

    private final CustomImageFont customImageFont;
    private final CustomImageFontStorage customImageFontStorage;
    private static final EmoteUtil instance = new EmoteUtil();

    private final ConcurrentHashMap<String, Integer> idToCodepointHashMap;

    private int currentCodepoint;

    private EmoteUtil() {
        this.idToCodepointHashMap = new ConcurrentHashMap<>();
        this.currentCodepoint = 1;

        /// The order is important here. Emote font storage depends on the emote font.
        this.customImageFont = new CustomImageFont();
        this.customImageFontStorage = new CustomImageFontStorage(this.getCustomImageFont());

        this.loadCache();
    }

    public void loadCache() {
        try {
            CustomImageCache.CacheEntry[] allCachedFiles = CustomImageCache.getInstance().getAllCachedFiles();
            for (var entry : allCachedFiles) {
                String id = entry.id();
                if (id.equals("nul_")) {
                    id = "nul";
                }
                InputStream is = new FileInputStream(entry.path().toFile());
                addImage(is, id, false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static EmoteUtil getInstance() {
        return instance;
    }
    public CustomImageFont getCustomImageFont() {
        return this.customImageFont;
    }

    public CustomImageFontStorage getCustomImageFontStorage() {
        return this.customImageFontStorage;
    }

    public void addEmote(String emoteName, NativeImage image, boolean writeToDisk) throws Exception {

        if (writeToDisk) {
            if (emoteName.equals("nul")) {
                image.writeToFile(CustomImageCache.getInstance().getPngFile("nul_"));
            } else {
                image.writeToFile(CustomImageCache.getInstance().getPngFile(emoteName));
            }
        }

        Integer codepoint = getCodepoint(emoteName);
        if (codepoint != null) {
            getCustomImageFont().removeGlyph(codepoint);
        } else {
            codepoint = getAndAdvanceCurrentCodepoint();
        }
        // advance is the amount the text is moved forward after the character
        int advance = (int) (image.getWidth() * CUSTOM_IMAGE_SCALE_FACTOR) + 1; // the +1 is to account for the shadow, which is a pixel in length
        // ascent is the height of the glyph relative to something
        int ascent = (int) (image.getHeight() * CUSTOM_IMAGE_SCALE_FACTOR);
        // both advance and ascent seem to correlate pretty well with its scale factor
        this.getCustomImageFont().addGlyph(codepoint,
                new CustomImageFont.CustomImageGlyph(CUSTOM_IMAGE_SCALE_FACTOR, image, 0, 0, image.getWidth(),
                        image.getHeight(), advance, ascent, emoteName));

        this.idToCodepointHashMap.put(emoteName, codepoint);
    }

    public void addImage(InputStream stream, String id, boolean writeToDisk) throws Exception {
        NativeImage image = NativeImage.read(stream);
        addEmote(id, image, writeToDisk);
    }

    private synchronized int getAndAdvanceCurrentCodepoint() {
        int prevCodepoint = currentCodepoint;
        currentCodepoint++;
        // Skip the space (' ') codepoint, because the TextRenderer does weird stuff with the space character
        // (like it doesn't get obfuscated and stuff).
        if (currentCodepoint == 32) currentCodepoint++;
        return prevCodepoint;
    }
    public Integer getCodepoint(String id) {
        return this.idToCodepointHashMap.getOrDefault(id, null);
    }
}
