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
