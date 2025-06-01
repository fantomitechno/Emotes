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
package dev.renoux.emotes.mixins;

import com.mojang.authlib.GameProfile;
import dev.renoux.emotes.utils.EmoteProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract void sendSystemMessage(Component message, boolean overlay);

    @Shadow protected abstract boolean acceptsChatMessages();

    // Messages from the server (/chat)
    @Inject(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(Component message, CallbackInfo ci) {
        if (message.toFlatList().isEmpty()) {
            if (message.getContents() instanceof TranslatableContents) {
                // If the message is a translation key, we don't want to process it
                this.sendSystemMessage(message, false);
            } else {
                // Process the message normally
                this.sendSystemMessage(EmoteProcessor.processMessage(message.getString(), message.getStyle()), false);
            }
        } else {
            this.sendSystemMessage(processSiblings(message.toFlatList()), false);
        }

        ci.cancel();
    }

    // Private messages (/msg, /tell, /w) and normal chat messages
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessage(OutgoingChatMessage message, boolean filterMaskEnabled, ChatType.Bound parameters, CallbackInfo ci) {
        if (message.content().getString().contains("xaero-waypoint"))
            return;
        ci.cancel();

        if (!this.acceptsChatMessages()) return;
        Component messageProcessed = EmoteProcessor.processMessage(message.content().getString(), message.content().getStyle());

        PlayerChatMessage newMessage = PlayerChatMessage.system(message.content().getString()).withUnsignedContent(messageProcessed);

        OutgoingChatMessage finalMessage = OutgoingChatMessage.create(newMessage.withUnsignedContent(messageProcessed));

        finalMessage.sendToPlayer((ServerPlayer) (Object) this, filterMaskEnabled, parameters);
    }

    @Unique
    private Component processSiblings(List<Component> siblings) {
        MutableComponent newComponent = Component.empty();
        for (Component sibling : siblings) {
            Component newSibling;
            if (!sibling.getSiblings().isEmpty()) {
                newSibling = processSiblings(sibling.toFlatList());
            } else {
                if (sibling.getContents() instanceof TranslatableContents) {
                    // If the sibling is a translation key, we don't want to process it
                    newSibling = sibling;
                } else {
                    // Process the sibling normally
                    newSibling = EmoteProcessor.processMessage(sibling.getString(), sibling.getStyle());
                }
            }
            newComponent.append(newSibling);
        }
        return newComponent;
    }
}