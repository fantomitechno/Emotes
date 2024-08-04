/*
 * MIT License
 *
 * Copyright (c) 2024 Simon RENOUX aka fantomitechno
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.renoux.emotes.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static dev.renoux.emotes.Emotes.LOGGER;
import static dev.renoux.emotes.Emotes.MODID;

public class EmoteUtil {

    public static final ResourceLocation CUSTOM_IMAGE_FONT_IDENTIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "emote_font");

    // I've found this is a pretty good scale factor for 24x24px Twitch emotes.
    public static final float CUSTOM_IMAGE_SCALE_FACTOR = 0.08f;

    private CustomImageFont customImageFont;
    private CustomImageFontStorage customImageFontStorage;
    private static final EmoteUtil instance = new EmoteUtil();

    private ConcurrentHashMap<String, Integer> idToCodepointHashMap;

    private int currentCodepoint;

    private boolean showSuggestions;

    public void setShowSuggestions(boolean value) {
        showSuggestions = value;
    }

    public boolean showSuggestions() {
        return showSuggestions;
    }

    private EmoteUtil() {
        this.idToCodepointHashMap = new ConcurrentHashMap<>();
        this.currentCodepoint = 1;

        /// The order is important here. Emote font storage depends on the emote font.
        this.customImageFont = new CustomImageFont();
        this.customImageFontStorage = new CustomImageFontStorage(this.getCustomImageFont());
    }

    public void reset() {
        this.idToCodepointHashMap = new ConcurrentHashMap<>();
        this.currentCodepoint = 1;
        this.showSuggestions = false;

        this.customImageFont.reset();
        this.customImageFontStorage.reset();
    }

    public void loadCache(String server) {
        this.getCustomImageFont().clear();
        try {
            CustomImageCache.CacheEntry[] allCachedFiles = CustomImageCache.getInstance().getAllCachedFiles(server);
            for (var entry : allCachedFiles) {
                String id = entry.id();
                if (id.equals("nul_")) {
                    id = "nul";
                }
                InputStream is = new FileInputStream(entry.path().toFile());
                addImage(server, is, id, false);
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

    public void addEmote(String server, String emoteName, NativeImage image, boolean writeToDisk) throws Exception {
        LOGGER.info("Registring emote " + emoteName);
        if (writeToDisk) {
            if (emoteName.equals("nul")) {
                image.writeToFile(CustomImageCache.getInstance().getPngFile(server, "nul_"));
            } else {
                image.writeToFile(CustomImageCache.getInstance().getPngFile(server, emoteName));
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

    public void addImage(String server, InputStream stream, String id, boolean writeToDisk) throws Exception {
        NativeImage image = NativeImage.read(stream);
        addEmote(server, id, image, writeToDisk);
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

    public List<String> getEmotes() {
        return this.idToCodepointHashMap.keySet().stream().toList();
    }
}
