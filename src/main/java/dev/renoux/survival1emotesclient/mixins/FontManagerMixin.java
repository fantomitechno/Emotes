/*
 * Copyright 2019 Pablo Pérez Rodríguez (pablo.rabanales@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.renoux.survival1emotesclient.mixins;

import com.mojang.blaze3d.font.GlyphProvider;
import dev.renoux.survival1emotesclient.util.EmoteUtil;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(FontManager.class)
public abstract class FontManagerMixin implements PreparableReloadListener, AutoCloseable {

  @Shadow @Final private Map<ResourceLocation, FontSet> fontSets;

  @Shadow @Final private List<GlyphProvider> providersToClose;

  @Inject(
    method = "apply",
    at=@At("TAIL")
  )
  private void reload(FontManager.Preparation preparation, ProfilerFiller profilerFiller, CallbackInfo ci) {
    System.out.println("Added CustomImageFont");
    this.providersToClose.add(EmoteUtil.getInstance().getCustomImageFont());
    this.fontSets.put(EmoteUtil.CUSTOM_IMAGE_FONT_IDENTIFIER, EmoteUtil.getInstance().getCustomImageFontStorage());
  }
}
