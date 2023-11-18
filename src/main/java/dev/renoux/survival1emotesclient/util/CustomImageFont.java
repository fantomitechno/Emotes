package dev.renoux.survival1emotesclient.util;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CustomImageFont implements GlyphProvider {
  private final CodepointMap<CustomImageGlyph> glyphs;

  public CustomImageFont() {
    this.glyphs = new CodepointMap<>(CustomImageGlyph[]::new, CustomImageGlyph[][]::new);
  }

  @Override
  public void close() {
//    this.image.close();
  }

  public synchronized void addGlyph(int codepoint, CustomImageGlyph glyph) {
    this.glyphs.put(codepoint, glyph);
  }

  public synchronized void removeGlyph(int codepoint) {
    this.glyphs.remove(codepoint);
  }

  @Override
  @Nullable
  public synchronized GlyphInfo getGlyph(int codePoint) {
    return this.glyphs.get(codePoint);
  }

  @Override
  public synchronized IntSet getSupportedGlyphs() {
    return IntSets.unmodifiable(this.glyphs.keySet());
  }

  public record CustomImageGlyph(float scaleFactor, NativeImage image, int x, int y, int width, int height, int advance, float ascent, String id) implements GlyphInfo
  {
    @Override
    public float getAdvance() {
      return this.advance;
    }

    @Override
    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
      return function.apply(new CustomImageRenderableGlyph(){
        @Override
        public float getOversample() {
          return 1.0f / scaleFactor;
        }

        @Override
        public int getPixelWidth() {
          return width;
        }

        @Override
        public int getPixelHeight() {
          return height;
        }

        @Override
        public float getBearingY() {
          return CustomImageRenderableGlyph.super.getBearingY() + 7.0f - ascent;
        }

        @Override
        public void upload(int x, int y) {
          image.upload(0, x, y, 0, 0, width, height, false, false);
        }

        @Override
        public boolean isColored() {
          return image.format().components() > 1;
        }

        @Override
        public String getId() {
          return id;
        }
      });
    }
  }
}

