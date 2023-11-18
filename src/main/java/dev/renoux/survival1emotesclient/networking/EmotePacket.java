package dev.renoux.survival1emotesclient.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;

import static dev.renoux.survival1emotesclient.Survival1EmotesClient.MODID;

public class EmotePacket implements Packet<ClientGamePacketListener> {
    public static final ResourceLocation PACKET = new ResourceLocation(MODID, "emote");

    public final byte[] emoteFile;
    public final String name;
    public EmotePacket(FriendlyByteBuf buf) {
        emoteFile = buf.readByteArray();
        name = buf.readUtf();
    }

    public EmotePacket(String name) {
        this.emoteFile = null;
        this.name = name;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
    }
}
