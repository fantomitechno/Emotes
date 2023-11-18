package dev.renoux.survival1emotesclient.util;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomImageFontStorage extends FontSet implements AutoCloseable {
  private static final RandomSource RANDOM = RandomSource.create();
  private static final float MAX_ADVANCE = 32.0f;
  private final ResourceLocation id;
  private final GlyphProvider font;
  private final CodepointMap<BakedGlyph> glyphRendererCache = new CodepointMap<>(BakedGlyph[]::new, BakedGlyph[][]::new);
  private final CodepointMap<GlyphPair> glyphCache = new CodepointMap<>(GlyphPair[]::new, GlyphPair[][]::new);
  private final Int2ObjectMap<IntList> charactersByWidth = new Int2ObjectOpenHashMap<IntList>();
  private final List<FontTexture> glyphAtlases = new ArrayList<>();

  public CustomImageFontStorage(CustomImageFont customImageFontInstance) {
    super(null, null);
    this.id = EmoteUtil.CUSTOM_IMAGE_FONT_IDENTIFIER;
    this.font = customImageFontInstance;
  }

  @Override
  public void reload(List<GlyphProvider> fonts) { }

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
    if (!(_c instanceof CustomImageRenderableGlyph)) {
      return super.missingGlyph;
    }
    CustomImageRenderableGlyph c = (CustomImageRenderableGlyph) _c;

    for (FontTexture glyphAtlasTexture : this.glyphAtlases) {
      BakedGlyph glyphRenderer = glyphAtlasTexture.add(c);
      if (glyphRenderer == null) continue;
      return glyphRenderer;
    }

    ResourceLocation identifier = this.id.withSuffix("/" + c.getId());
    boolean bl = c.isColored();

    GlyphRenderTypes textRenderLayerSet = bl ? GlyphRenderTypes.createForColorTexture(identifier) : GlyphRenderTypes.createForIntensityTexture(identifier);
    FontTexture glyphAtlasTexture2 = new FontTexture(textRenderLayerSet, bl);
    this.glyphAtlases.add(glyphAtlasTexture2);

    Minecraft.getInstance().getTextureManager().register(identifier, glyphAtlasTexture2);
    BakedGlyph glyphRenderer2 = glyphAtlasTexture2.add(c);
    return glyphRenderer2 == null ? super.missingGlyph : glyphRenderer2;
  }

  @Override
  public @NotNull BakedGlyph getRandomGlyph(GlyphInfo glyph) {
    IntList intList = (IntList)this.charactersByWidth.get(Mth.ceil(glyph.getAdvance(false)));
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
