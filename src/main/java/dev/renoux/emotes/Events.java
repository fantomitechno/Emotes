package dev.renoux.emotes;

import com.mojang.blaze3d.platform.NativeImage;
import dev.renoux.emotes.networking.AskEmotePacket;
import dev.renoux.emotes.networking.EmotePacket;
import dev.renoux.emotes.networking.ListEmotePacket;
import dev.renoux.emotes.networking.Packet;
import dev.renoux.emotes.utils.CustomImageCache;
import dev.renoux.emotes.utils.EmoteUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.config.api.values.ValueList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static dev.renoux.emotes.Emotes.LOGGER;
import static dev.renoux.emotes.Emotes.metadata;

public class Events {
    private static String nameAndHashArray;
    private static HashMap<String, byte[]> emotesFiles;

    private static Boolean registeredPayload = false;

    public static void init(Boolean client) throws IOException {
        if (!registeredPayload) loadPayloadRegistry();
        if (client) {
            initClientCustomPlayload();
            initClientLogin();
        } else {
            initPlayerJoin();
            initCustomPayload();

            ValueList<String> emotes = Emotes.serverConfig.emotes.getRealValue();
            emotesFiles = new HashMap<>();
            StringBuilder nameAndHash = new StringBuilder();
            for (String emote : emotes) {
                String[] splitEmote = emote.split(":");
                String path = "config/" + metadata.getId() + "/emotes/";
                if (splitEmote[0].equals("nul")) {
                    path += "nul_.png";
                } else {
                    path += splitEmote[0] + ".png";
                }
                File file = new File(path);
                if (file.exists()) {
                    try {
                        emotesFiles.put(splitEmote[0], new FileInputStream(file).readAllBytes());
                        nameAndHash.append(splitEmote[0]).append(":").append(file.hashCode()).append(",");
                    } catch (Exception exception) {
                        LOGGER.info("{} : An error occurred while loading {} emote: {}", metadata.getName(), emote, exception.getMessage());
                        throw new IOException("An error occurred while loading \"" + emote + "\" emote: " + exception.getMessage());
                    }
                } else {
                    throw new IOException("An image for the emote \"" + splitEmote[0] + "\" is missing");
                }
            }
            nameAndHashArray = nameAndHash.toString();
        }
    }

    private static void loadPayloadRegistry() {
        PayloadTypeRegistry.playS2C().register(ListEmotePacket.PACKET, createCode(ListEmotePacket.class));
        PayloadTypeRegistry.playS2C().register(EmotePacket.PACKET, createCode(EmotePacket.class));
        PayloadTypeRegistry.playC2S().register(AskEmotePacket.PACKET, createCode(AskEmotePacket.class));
        registeredPayload = true;
    }

    private static void initPlayerJoin() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(new ListEmotePacket(nameAndHashArray)));
    }

    private static void initCustomPayload() {
        ServerPlayNetworking.registerGlobalReceiver(AskEmotePacket.PACKET, (payload, context) -> context.responseSender().sendPacket(new EmotePacket(emotesFiles.get(payload.name), payload.name)));
    }

    private static void initClientCustomPlayload() {
        ClientPlayNetworking.registerGlobalReceiver(EmotePacket.PACKET, (payload, context) -> {
            String ip = sanitizeIP(Objects.requireNonNull(Minecraft.getInstance().getCurrentServer()).ip);
            try {
                EmoteUtil.getInstance().addEmote(ip, payload.name, NativeImage.read(new ByteArrayInputStream(payload.emoteFile)), true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ListEmotePacket.PACKET, (payload, context) -> {
            String ip = sanitizeIP(Objects.requireNonNull(Minecraft.getInstance().getCurrentServer()).ip);

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
                    LOGGER.info("{} : Removing {}", metadata.getName(), cacheEntry.id());
                }

                for (String unknowEmote : unknownEmotes) {
                    LOGGER.info("{} : Asking for {}", metadata.getName(), unknowEmote);
                    context.responseSender().sendPacket(new ServerboundCustomPayloadPacket(new AskEmotePacket(unknowEmote)));
                }

                EmoteUtil.getInstance().loadCache(ip);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    private static void initClientLogin() {
        ClientConfigurationConnectionEvents.COMPLETE.register((handler, client) -> EmoteUtil.getInstance().reset());
    }

    private static <T extends Packet<T>> StreamCodec<RegistryFriendlyByteBuf, T> createCode(Class<T> packetType) {
        return new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, T packet) {
                packet.toBytes(buf);
            }

            @Override
            public @NotNull T decode(RegistryFriendlyByteBuf buf) {
                try {
                    T packet = packetType.getDeclaredConstructor().newInstance();
                    packet.fromBytes(buf);
                    return packet;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static String sanitizeIP(String ip) {
        return ip.replaceAll("\\.", "_").replaceAll(":", "_");
    }
}
