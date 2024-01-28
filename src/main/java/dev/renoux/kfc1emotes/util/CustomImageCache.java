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
package dev.renoux.kfc1emotes.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.renoux.kfc1emotes.KFC1Emotes.MODID;

public class CustomImageCache {
    private static CustomImageCache instance;

    private final Path cacheFolder;

    CustomImageCache() {
        this.cacheFolder = FabricLoader
                .getInstance()
                .getConfigDir()
                .resolve(MODID)
                .resolve("cache");
        this.cacheFolder.toFile().mkdirs();
    }

    public File getFile(String server, String pathStr) {
        Path path = this.cacheFolder.resolve(server).resolve(pathStr);
        path.getParent().toFile().mkdirs();
        return path.toFile();
    }
    public File getPngFile(String server, String customImageId) {
        return this.getFile(server, customImageId + ".png");
    }

    public CacheEntry[] getAllCachedFiles(String server) throws IOException {
        this.cacheFolder.resolve(server).toFile().mkdirs();
        return Files.walk(this.cacheFolder.resolve(server))
                .filter(p -> p.toFile().isFile() && p.toString().endsWith(".png"))
                .map(p -> new CacheEntry(this.cacheFolder.resolve(server).relativize(p).toString().replace(".png", ""), p))
                .toArray(CacheEntry[]::new);
    }

    public static CustomImageCache getInstance() {
        if (instance == null) {
            instance = new CustomImageCache();
        }
        return instance;
    }

    public record CacheEntry(String id, Path path) { }
}
