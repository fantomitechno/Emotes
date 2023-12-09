package dev.renoux.survival1emotesclient.mixins;

import dev.renoux.survival1emotesclient.util.EmojiSuggestionHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Unique
    private EmojiSuggestionHelper emojiSuggestionHelper;

    @Inject(method = "init", at = @At("HEAD"))
    protected void init(CallbackInfo ci) {
        emojiSuggestionHelper = new EmojiSuggestionHelper((ChatScreen) ((Object) this));
    }


    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        if (emojiSuggestionHelper != null) emojiSuggestionHelper.render(guiGraphics);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (emojiSuggestionHelper != null && emojiSuggestionHelper.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
