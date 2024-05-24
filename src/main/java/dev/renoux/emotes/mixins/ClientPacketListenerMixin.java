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

import com.mojang.blaze3d.platform.NativeImage;
import dev.renoux.emotes.Emotes;
import dev.renoux.emotes.networking.EmotePacket;
import dev.renoux.emotes.networking.ListEmotePacket;
import dev.renoux.emotes.util.CustomImageCache;
import dev.renoux.emotes.util.EmoteUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow public abstract void send(Packet<?> packet);

    @Shadow
    public abstract @Nullable ServerData getServerData();

    @Unique
    private String sanatizeIP(String ip) {
        return ip.replaceAll("\\.", "_").replaceAll(":", "_");
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket, CallbackInfo ci) throws Exception {
        if (Minecraft.getInstance().getCurrentServer() == null) return;
        String ip = sanatizeIP(Minecraft.getInstance().getCurrentServer().ip);
        if (clientboundCustomPayloadPacket.getIdentifier().equals(EmotePacket.PACKET)) {
            EmotePacket packet = new EmotePacket(clientboundCustomPayloadPacket.getData());
            Emotes.LOGGER.info("Got emote " + packet.name);
            EmoteUtil.getInstance().addEmote(ip, packet.name, NativeImage.read(packet.emoteFile), true);
            ci.cancel();
        } else if (clientboundCustomPayloadPacket.getIdentifier().equals(ListEmotePacket.PACKET)) {
            ListEmotePacket packet = new ListEmotePacket(clientboundCustomPayloadPacket.getData());

            List<CustomImageCache.CacheEntry> cached = new java.util.ArrayList<>(List.of(CustomImageCache.getInstance().getAllCachedFiles(ip)));

            List<String> unknowEmotes = new ArrayList<>();

            for (String name : packet.nameAndHash.keySet()) {
                if (name.equals("nul")) {
                    if (!cached.removeIf(c -> c.id().equals("nul_"))) {
                        unknowEmotes.add(name);
                    }
                } else {
                    if (!cached.removeIf(c -> c.id().equals(name))) {
                        unknowEmotes.add(name);
                    }
                }
            }

            for (CustomImageCache.CacheEntry cacheEntry : cached) {
                new File(cacheEntry.path().toString()).delete();
                Emotes.LOGGER.info("Removing " + cacheEntry.id());
            }

            for (String unknowEmote : unknowEmotes) {
                Emotes.LOGGER.info("Asking for " + unknowEmote);
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                new EmotePacket(unknowEmote).write(buf);
                this.send(new ServerboundCustomPayloadPacket(EmotePacket.PACKET, buf));
            }
            
            ci.cancel();
        }
    }

    @Inject(method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V", at = @At("HEAD"))
    private void joinServer(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci) {
        EmoteUtil.getInstance().reset();
        if (this.getServerData() != null)
            EmoteUtil.getInstance().loadCache(sanatizeIP(this.getServerData().ip));
    }
}
