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
package dev.renoux.emotes;

import com.mojang.blaze3d.platform.NativeImage;
import dev.renoux.emotes.networking.EmotePacket;
import dev.renoux.emotes.networking.ListEmotePacket;
import dev.renoux.emotes.networking.Packet;
import dev.renoux.emotes.util.CustomImageCache;
import dev.renoux.emotes.util.EmoteUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import org.spongepowered.asm.mixin.Unique;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Emotes implements ModInitializer {
  public static final String MODID = "emotes";
  public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

  @Override
  public void onInitialize() {
    LOGGER.info("Emotes : LOADING");


    PayloadTypeRegistry.playS2C().register(EmotePacket.PACKET, createCode(EmotePacket.class));
    ClientPlayNetworking.registerGlobalReceiver(EmotePacket.PACKET, (payload, context) -> {
      String ip = sanitizeIP(Minecraft.getInstance().getCurrentServer().ip);
      try {
          EmoteUtil.getInstance().addEmote(ip, payload.name, NativeImage.read(payload.emoteFile), true);
      } catch (Exception e) {
          throw new RuntimeException(e);
      }
    });

    PayloadTypeRegistry.playS2C().register(ListEmotePacket.PACKET, createCode(ListEmotePacket.class));
    ClientPlayNetworking.registerGlobalReceiver(ListEmotePacket.PACKET, (payload, context) -> {
      String ip = sanitizeIP(Minecraft.getInstance().getCurrentServer().ip);

        try {
          List<CustomImageCache.CacheEntry> cached = new ArrayList<>(List.of(CustomImageCache.getInstance().getAllCachedFiles(ip)));

          List<String> unknownEmotes = new ArrayList<>();

          for (String name : payload.nameAndHash.keySet()) {
            if (name.equals("nul")) {
              if (!cached.removeIf(c -> c.id().equals("nul_"))) {
                unknownEmotes.add(name);
              }
            } else {
              if (!cached.removeIf(c -> c.id().equals(name))) {
                unknownEmotes.add(name);
              }
            }
          }

          for (CustomImageCache.CacheEntry cacheEntry : cached) {
            new File(cacheEntry.path().toString()).delete();
            Emotes.LOGGER.info("Removing " + cacheEntry.id());
          }

          for (String unknowEmote : unknownEmotes) {
            Emotes.LOGGER.info("Asking for " + unknowEmote);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            context.responseSender().sendPacket(new ServerboundCustomPayloadPacket(new EmotePacket(unknowEmote)));
          }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    });

    LOGGER.info("Emotes : LOADED");
  }

  public <T extends Packet<T>> StreamCodec<RegistryFriendlyByteBuf, T> createCode(Class<T> packetType) {
    return new StreamCodec<>() {

      @Override
      public void encode(RegistryFriendlyByteBuf buf, T packet) {
        packet.toBytes(buf);
      }

      @Override
      public T decode(RegistryFriendlyByteBuf buf) {
        try {
          T packet = packetType.getDeclaredConstructor().newInstance();
          packet.fromBytes(buf);
          return packet;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  };

  @Unique
  private String sanitizeIP(String ip) {
    return ip.replaceAll("\\.", "_").replaceAll(":", "_");
  }
}
