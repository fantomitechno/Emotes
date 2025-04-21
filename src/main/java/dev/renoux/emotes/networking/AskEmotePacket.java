package dev.renoux.emotes.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static dev.renoux.emotes.Emotes.metadata;

public class AskEmotePacket implements Packet<AskEmotePacket> {
    public static final Type<AskEmotePacket> PACKET = new Type<>(ResourceLocation.fromNamespaceAndPath(metadata.getId(), "ask_emotes"));
    public String name;

    @SuppressWarnings("unused")
    public AskEmotePacket() {
    }

    public AskEmotePacket(String name) {
        this.name = name;
    }

    @Override
    public AskEmotePacket fromBytes(FriendlyByteBuf buf) {
        name = buf.readUtf();

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    @Override
    public CustomPacketPayload.Type<AskEmotePacket> type() {
        return PACKET;
    }
}
