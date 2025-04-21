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
package dev.renoux.emotes.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static dev.renoux.emotes.Emotes.metadata;

public class EmotePacket implements Packet<EmotePacket> {
    public static final Type<EmotePacket> PACKET = new Type<>(ResourceLocation.fromNamespaceAndPath(metadata.getId(), "emotes"));

    public byte[] emoteFile;
    public String name;

    @SuppressWarnings("unused")
    public EmotePacket() {
    }

    public EmotePacket(byte[] emoteFile, String name) {
        this.emoteFile = emoteFile;
        this.name = name;
    }

    @Override
    public EmotePacket fromBytes(FriendlyByteBuf buf) {
        name = buf.readUtf();
        emoteFile = buf.readByteArray();

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeByteArray(emoteFile);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<EmotePacket> type() {
        return PACKET;
    }
}
