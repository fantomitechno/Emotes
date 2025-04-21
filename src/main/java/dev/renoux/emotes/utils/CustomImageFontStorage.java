/*
 * Copyright 2019 Pablo Pérez Rodríguez (pablo.rabanales@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.renoux.emotes.utils;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.*;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomImageFontStorage extends FontSet implements AutoCloseable {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final float MAX_ADVANCE = 32.0f;
    private final ResourceLocation id;
    private final GlyphProvider font;
    private CodepointMap<BakedGlyph> glyphRendererCache = new CodepointMap<>(BakedGlyph[]::new, BakedGlyph[][]::new);
    private CodepointMap<GlyphPair> glyphCache = new CodepointMap<>(GlyphPair[]::new, GlyphPair[][]::new);
    private Int2ObjectMap<IntList> charactersByWidth = new Int2ObjectOpenHashMap<>();
    private List<FontTexture> glyphAtlases = new ArrayList<>();

    public CustomImageFontStorage(CustomImageFont customImageFontInstance) {
        super(null, null);
        this.id = EmoteUtil.CUSTOM_IMAGE_FONT_IDENTIFIER;
        this.font = customImageFontInstance;
    }

    public void reset() {
        this.glyphRendererCache = new CodepointMap<>(BakedGlyph[]::new, BakedGlyph[][]::new);
        this.glyphCache = new CodepointMap<>(GlyphPair[]::new, GlyphPair[][]::new);
        this.charactersByWidth = new Int2ObjectOpenHashMap<>();
        this.glyphAtlases = new ArrayList<>();
    }

    @Override
    public void reload(List<GlyphProvider.Conditional> fonts, Set<FontOption> activeFilters) { }

    @Override
    public void close() {
        // this.closeFont();
        // this.closeGlyphAtlases();
    }

    private void closeFont() {
        font.close();
//    this.fonts.clear();
    }

    private void closeGlyphAtlases() {
        for (FontTexture glyphAtlasTexture : this.glyphAtlases) {
            glyphAtlasTexture.close();
        }
        this.glyphAtlases.clear();
    }

    private static boolean isAdvanceInvalid(GlyphInfo glyph) {
        float f = glyph.getAdvance(false);
        if (f < 0.0f || f > MAX_ADVANCE) {
            return true;
        }
        float g = glyph.getAdvance(true);
        return g < 0.0f || g > MAX_ADVANCE;
    }

    /**
     * {@return the glyph of {@code codePoint}}
     *
     * @apiNote Call {@link #getGlyph} instead, as that method provides caching.
     */
    private GlyphPair findGlyph(int codePoint) {
        GlyphInfo glyph = font.getGlyph(codePoint);
        if (glyph == null)
            return GlyphPair.MISSING;
        if (CustomImageFontStorage.isAdvanceInvalid(glyph))
            return new GlyphPair(glyph, SpecialGlyphs.MISSING);

        return new GlyphPair(glyph, glyph);
    }
    @Override
    public @NotNull GlyphInfo getGlyphInfo(int codePoint, boolean validateAdvance) {
        return this.glyphCache.computeIfAbsent(codePoint, this::findGlyph).getGlyph(validateAdvance);
    }

    private BakedGlyph findGlyphRenderer(int codePoint) {
        GlyphInfo glyph = font.getGlyph(codePoint);
        if (glyph != null)
            return glyph.bake(this::getGlyphRenderer);
        return super.missingGlyph;
    }

    @Override
    public @NotNull BakedGlyph getGlyph(int codePoint) {
        return this.glyphRendererCache.computeIfAbsent(codePoint, this::findGlyphRenderer);
    }

    private BakedGlyph getGlyphRenderer(SheetGlyphInfo _c) {
        if (!(_c instanceof CustomImageRenderableGlyph c)) {
            return super.missingGlyph;
        }

        for (FontTexture glyphAtlasTexture : this.glyphAtlases) {
            BakedGlyph glyphRenderer = glyphAtlasTexture.add(c);
            if (glyphRenderer == null) continue;
            return glyphRenderer;
        }

        ResourceLocation identifier = this.id.withSuffix("/" + c.getId());
        boolean bl = c.isColored();

        GlyphRenderTypes textRenderLayerSet = bl ? GlyphRenderTypes.createForColorTexture(identifier) : GlyphRenderTypes.createForIntensityTexture(identifier);
        FontTexture glyphAtlasTexture2 = new FontTexture(this.id::getPath, textRenderLayerSet, bl);
        this.glyphAtlases.add(glyphAtlasTexture2);

        Minecraft.getInstance().getTextureManager().register(identifier, glyphAtlasTexture2);
        BakedGlyph glyphRenderer2 = glyphAtlasTexture2.add(c);
        return glyphRenderer2 == null ? super.missingGlyph : glyphRenderer2;
    }

    @Override
    public @NotNull BakedGlyph getRandomGlyph(GlyphInfo glyph) {
        IntList intList = this.charactersByWidth.get(Mth.ceil(glyph.getAdvance(false)));
        if (intList != null && !intList.isEmpty()) {
            return this.findGlyphRenderer(intList.getInt(RANDOM.nextInt(intList.size())));
        }
        return super.missingGlyph;
    }

    @Override
    public @NotNull BakedGlyph whiteGlyph() {
        return super.whiteGlyph();
    }

    record GlyphPair(GlyphInfo glyph, GlyphInfo advanceValidatedGlyph) {
        static final GlyphPair MISSING = new GlyphPair(SpecialGlyphs.MISSING, SpecialGlyphs.MISSING);

        GlyphInfo getGlyph(boolean validateAdvance) {
            return validateAdvance ? this.advanceValidatedGlyph : this.glyph;
        }
    }
}
