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
package dev.renoux.emotes.utils;

import dev.renoux.emotes.Emotes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.quiltmc.config.api.values.ValueList;

import java.util.HashMap;
import java.util.Map;

public class EmoteProcessor {

    private static Map<String, String> EMOTES = new HashMap<>();

    public static void init() {
        EMOTES = new HashMap<>();
        ValueList<String> emotes = Emotes.serverConfig.emotes.getRealValue();
        for (String emote : emotes) {
            String[] splitedEmote = emote.split(":");
            EMOTES.put(splitedEmote[0], splitedEmote[1]);
        }
    }

    public static Component processMessage(String message, Style format) {
        char[] chars = message.toCharArray();
        StringBuilder textBuilder = new StringBuilder();
        MutableComponent rootElement = null;
        MutableComponent currentElement = null;

        int index = 0;
        int emoteStartIndex = 0;
        boolean readingEmoteName = false;
        while (index < chars.length) {
            char token = chars[index++];

            textBuilder.append(token);

            if (token == ':') { // Found start/end of emote pattern
                if (!readingEmoteName) { // Not currently reading emote name, means this *may* be the beginning of an
                    // emote
                    readingEmoteName = true;
                    emoteStartIndex = textBuilder.length() - 1;
                } else { // Currently reading emote name, meaning that this is the end of an emote and
                    // should be processed as an emote
                    readingEmoteName = false;

                    String emoteName = textBuilder.substring(emoteStartIndex + 1, textBuilder.length() - 1);
                    String emote = EMOTES.get(emoteName);
                    if (emote != null) { // Valid emote name, proceeding translation
                        // Strip emote from text builder
                        textBuilder.delete(emoteStartIndex, textBuilder.length());

                        if (!textBuilder.isEmpty()) {
                            MutableComponent text = Component.literal(textBuilder.toString());

                            if (currentElement != null) {
                                currentElement.append(text);
                            }

                            currentElement = text;

                            if (rootElement == null) {
                                rootElement = currentElement;
                            }
                        }

                        MutableComponent emoteText = Component.translatableWithFallback("emotes." + emoteName, emote);

                        if (currentElement != null) {
                            currentElement.append(emoteText);
                        }

                        currentElement = emoteText;

                        if (rootElement == null) {
                            rootElement = currentElement;
                        }

                        textBuilder.delete(0, textBuilder.length()); // Clear string builder
                    }
                }
            }
        }

        if (!textBuilder.isEmpty()) {
            MutableComponent text = Component.literal(textBuilder.toString());

            if (currentElement != null) {
                currentElement.append(text);
            }

            currentElement = text;

            if (rootElement == null) {
                rootElement = currentElement;
            }
        }

        assert rootElement != null;
        return rootElement.setStyle(format);
    }
}