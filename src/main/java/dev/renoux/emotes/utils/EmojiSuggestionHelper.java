/*
 * Copyright 2019 Pablo Pérez Rodríguez (pablo.rabanales@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
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

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.codec.binary.StringUtils;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiSuggestionHelper extends IDrawableGuiListener {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");

    private final ChatScreen chatScreen;
    private CompletableFuture<Suggestions> suggestionsFuture;
    private EmojiSuggestions suggestions;
    private boolean updating;
    private String inputFieldTextLast;

    public EmojiSuggestionHelper(ChatScreen  screen) {
        this.chatScreen = screen;
        this.updating = false;
    }

    private static Suggestions createSuggestions(final Iterable<String> collection, final SuggestionsBuilder suggestionBuilder) {
        final String remaining = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (String key : collection) {
            String modifiedKey = ":" + key + ":";
            if (modifiedKey.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                suggestionBuilder.suggest(modifiedKey);
            }
        }
        return suggestionBuilder.build();
    }

    private static int getLastWordIndex(String p_228121_0_) {
        if (Strings.isNullOrEmpty(p_228121_0_)) {
            return 0;
        } else {
            int i = 0;

            for (Matcher matcher = WHITESPACE_PATTERN.matcher(p_228121_0_); matcher.find(); i = matcher.end()) {

            }

            return i;
        }
    }

    private static String trim(String text, String textAll) {
        return textAll.startsWith(text) ? textAll.substring(text.length()) : null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.suggestions != null && this.suggestions.onKeyPressed(keyCode)) {
            return true;
        } else if (keyCode == 258) {
            this.updateSuggestionList(false);
            return suggestions != null;
        } else {
            return false;
        }
    }

    public void updateSuggestionList(boolean skip) {
        final String s = this.chatScreen.input.getValue();

        if (!this.updating) {
            this.chatScreen.input.setSuggestion(null);
            this.suggestions = null;
        }

        final StringReader stringreader = new StringReader(s);
        if (stringreader.canRead()) {
            final int cursorPosition = this.chatScreen.input.getCursorPosition();
            final int lastWordIndex = getLastWordIndex(s);
            if (lastWordIndex < s.length() ? s.charAt(lastWordIndex) == ':' : !s.isEmpty() && s.charAt(0) == ':') {
                if ((skip || cursorPosition - lastWordIndex >= 1) && (this.suggestions == null || !this.updating)) {
                    final CompletableFuture<Iterable<String>> list = CompletableFuture.supplyAsync(() -> EmoteUtil.getInstance().getEmotes());
                    this.suggestionsFuture = list.thenApplyAsync(stringIterable -> createSuggestions(stringIterable, new SuggestionsBuilder(s, lastWordIndex)));
                    this.suggestionsFuture.thenRun(() -> {
                        if (this.suggestionsFuture.isDone())
                            showSuggestions();
                    });
                }
            }
        }
    }

    public void showSuggestions() {
        if (this.suggestionsFuture != null && this.suggestionsFuture.isDone()) {
            int i = 0;
            final Suggestions suggestions = this.suggestionsFuture.join();
            if (!suggestions.getList().isEmpty()) {
                for (final Suggestion suggestion : suggestions.getList())
                    i = Math.max(i, Minecraft.getInstance().font.width(suggestion.getText()));
                final int j = Minecraft.getInstance().font.width(this.chatScreen.input.getValue().substring(0, this.chatScreen.input.getCursorPosition() - suggestions.getRange().getLength() + 2));
                this.suggestions = new EmojiSuggestions(j, this.chatScreen.height - 12, i, suggestions);
            }
        }
    }

    public void render(GuiGraphics matrixStack) {
        if (this.suggestions != null) {
            this.suggestions.render(matrixStack);
        }
        checkTextUpdate();
    }

    public void checkTextUpdate() {
        final String inputFieldText = this.chatScreen.input.getValue();
        if (!StringUtils.equals(this.inputFieldTextLast, inputFieldText)) {
            this.inputFieldTextLast = inputFieldText;
            updateSuggestionList(false);
        }
    }

    public class EmojiSuggestions {

        private final Rect2i area;
        private final Suggestions suggestions;
        private final String currentText;
        private int index;

        public EmojiSuggestions(int x, int y, int areaWidth, Suggestions suggestions) {
            this.suggestions = suggestions;
            int height = Math.min(suggestions.getList().size(), 10) * (Minecraft.getInstance().font.lineHeight + 3);
            this.area = new Rect2i(x - 1, y - 3 - height, areaWidth, height);
            this.currentText = EmojiSuggestionHelper.this.chatScreen.input.getValue();
            setIndex(0);
        }

        public void render(GuiGraphics guiGraphics) {
            for (int i = 0; i < Math.min(this.suggestions.getList().size(), 10); ++i) {
                int pos = (this.index + i) % this.suggestions.getList().size();
                final Suggestion suggestion = this.suggestions.getList().get(pos);
                guiGraphics.fill(this.area.getX(), this.area.getY() + 12 * i, this.area.getX() + this.area.getWidth() + 15, this.area.getY() + 12 * i + 12, 0xD0000000);
                String emote = Character.toString(EmoteUtil.getInstance().getCodepoint(suggestion.getText().replaceAll(":", "")));
                Component emoteComp = Component.literal(emote).withStyle(Style.EMPTY.withFont(EmoteUtil.CUSTOM_IMAGE_FONT_IDENTIFIER));
                guiGraphics.drawString(Minecraft.getInstance().font, emoteComp, this.area.getX() + 1, this.area.getY() + 2 + 12 * i, pos == this.index ? 0xFFFFFF00 : 0xFFAAAAAA, false);
                guiGraphics.drawString(Minecraft.getInstance().font, suggestion.getText(), 12 + this.area.getX() + 1, this.area.getY() + 2 + 12 * i, pos == this.index ? 0xFFFFFF00 : 0xFFAAAAAA, true);
            }
        }

        public void setIndex(int i) {
            this.index = i;
            if (this.index < 0) this.index = this.suggestions.getList().size() - 1;
            else if (this.index >= this.suggestions.getList().size()) this.index = 0;
            EmojiSuggestionHelper.this.chatScreen.input.setSuggestion(trim(EmojiSuggestionHelper.this.chatScreen.input.getValue(), suggestions.getList().get(this.index).apply(currentText)));
        }

        public boolean onKeyPressed(int keyCode) {
            if (keyCode == 265) {
                offsetIndex(-1);
                return true;
            } else if (keyCode == 264) {
                offsetIndex(1);
                return true;
            } else if (keyCode == 258) {
                applySuggestion();
                return true;
            } else if ((keyCode == 257 || keyCode == 335)) {
                applySuggestion();
                return true;
            } else if (keyCode == 256) {
                removeSuggestion();
                return true;
            } else if ((keyCode == 262 || keyCode == 263)) {
                EmojiSuggestionHelper.this.chatScreen.input.setSuggestion("");
                removeSuggestion();
                return false;
            } else
                return false;
        }

        public void offsetIndex(final int deltaIndex) {
            setIndex(this.index + deltaIndex);
        }

        public void removeSuggestion() {
            EmojiSuggestionHelper.this.suggestions = null;
        }

        public void applySuggestion() {
            final Suggestion suggestion = this.suggestions.getList().get(this.index);
            EmojiSuggestionHelper.this.updating = true;
            EmojiSuggestionHelper.this.chatScreen.input.setValue(suggestion.apply(this.currentText));
            final int i = suggestion.getRange().getStart() + suggestion.getText().length();
            EmojiSuggestionHelper.this.chatScreen.input.moveCursorTo(i, false);
            EmojiSuggestionHelper.this.chatScreen.input.setHighlightPos(i);
            setIndex(this.index);
            EmojiSuggestionHelper.this.updating = false;
        }
    }
}
