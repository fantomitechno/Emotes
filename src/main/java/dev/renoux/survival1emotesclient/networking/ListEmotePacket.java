package dev.renoux.survival1emotesclient.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static dev.renoux.survival1emotesclient.Survival1EmotesClient.MODID;

public class ListEmotePacket  implements Packet<ClientGamePacketListener> {
    public static final ResourceLocation PACKET = new ResourceLocation(MODID, "emote_list");

    public final Map<String, Integer> nameAndHash;
    public ListEmotePacket(FriendlyByteBuf buf) {
        this.nameAndHash = new HashMap<>();
        String nameAndHashArray = buf.readUtf();
        for (String nameHash: nameAndHashArray.split(",")) {
            if (!nameHash.isEmpty()) {
                String[] split = nameHash.split(":");
                this.nameAndHash.put(split[0], Integer.valueOf(split[1]));
            }
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
    }
}
