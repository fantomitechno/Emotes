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
package dev.renoux.survival1emotesclient.mixins;

import dev.renoux.survival1emotesclient.util.EmoteUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(TranslatableContents.class)
public class TranslatableContentsMixin {
    @Redirect(method = "decompose()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private String getOrDefault(Language instance, String key, String fallback) {
        if (key.startsWith("emotes.")) {
            return key + ":" + fallback;
        }
        return instance.getOrDefault(key, fallback);
    }

    @Redirect(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/FormattedText;visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;"))
    private <T> Optional<T> visit(FormattedText instance, FormattedText.StyledContentConsumer<T> tStyledContentConsumer, Style style) {
        if (instance.getString().startsWith("emotes.")) {
            String[] splitedEmote = instance.getString().split(":");
            String emote = Character.toString(EmoteUtil.getInstance().getCodepoint(splitedEmote[0].replace("emotes.", "")));
            FormattedText newInstance = FormattedText.of(emote);
            return newInstance.visit(tStyledContentConsumer, style.withFont(EmoteUtil.CUSTOM_IMAGE_FONT_IDENTIFIER).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(splitedEmote[1]))));
        }
        return instance.visit(tStyledContentConsumer, style);
    }
}
