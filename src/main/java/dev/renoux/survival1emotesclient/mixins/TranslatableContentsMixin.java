package dev.renoux.survival1emotesclient.mixins;

import dev.renoux.survival1emotesclient.util.EmoteUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(TranslatableContents.class)
public class TranslatableContentsMixin {

    @Shadow @Final private @Nullable String fallback;

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
