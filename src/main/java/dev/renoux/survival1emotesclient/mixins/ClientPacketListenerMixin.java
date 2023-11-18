package dev.renoux.survival1emotesclient.mixins;

import com.mojang.blaze3d.platform.NativeImage;
import dev.renoux.survival1emotesclient.Survival1EmotesClient;
import dev.renoux.survival1emotesclient.networking.EmotePacket;
import dev.renoux.survival1emotesclient.networking.ListEmotePacket;
import dev.renoux.survival1emotesclient.util.CustomImageCache;
import dev.renoux.survival1emotesclient.util.EmoteUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow public abstract void send(Packet<?> packet);

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket, CallbackInfo ci) throws Exception {
        if (clientboundCustomPayloadPacket.getIdentifier().equals(EmotePacket.PACKET)) {
            EmotePacket packet = new EmotePacket(clientboundCustomPayloadPacket.getData());
            Survival1EmotesClient.LOGGER.info("Got emote " + packet.name);
            EmoteUtil.getInstance().addEmote(packet.name, NativeImage.read(packet.emoteFile), true);
            ci.cancel();
        } else if (clientboundCustomPayloadPacket.getIdentifier().equals(ListEmotePacket.PACKET)) {
            ListEmotePacket packet = new ListEmotePacket(clientboundCustomPayloadPacket.getData());

            List<CustomImageCache.CacheEntry> cached = new java.util.ArrayList<>(List.of(CustomImageCache.getInstance().getAllCachedFiles()));

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
                Survival1EmotesClient.LOGGER.info("Removing " + cacheEntry.id());
            }

            for (String unknowEmote : unknowEmotes) {
                Survival1EmotesClient.LOGGER.info("Asking for " + unknowEmote);
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                new EmotePacket(unknowEmote).write(buf);
                this.send(new ServerboundCustomPayloadPacket(EmotePacket.PACKET, buf));
                Survival1EmotesClient.LOGGER.info("Asked for " + unknowEmote);
            }
            
            ci.cancel();
        }
    }
}
