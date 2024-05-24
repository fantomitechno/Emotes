/*
 * Copyright 2019 Pablo Pérez Rodríguez (pablo.rabanales@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.renoux.emotes.mixins;

import dev.renoux.emotes.util.EmojiSuggestionHelper;
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
