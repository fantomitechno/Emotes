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
