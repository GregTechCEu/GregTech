package gregtech.api.gui.resources.utils;

import net.minecraft.client.Minecraft;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TextureCache {

    private File cacheDirectory = new File(Minecraft.getMinecraft().gameDir, "opframe_cache");
    private File index = new File(cacheDirectory, "index");

    private Map<String, CacheEntry> entries = new HashMap<>();

    public TextureCache() {
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
        loadIndex();
    }

    public void save(String url, String etag, long time, long expireTime, byte[] data) {
        CacheEntry entry = new CacheEntry(url, etag, time, expireTime);
        boolean saved = false;
        OutputStream out = null;
        try {
            out = Files.newOutputStream(entry.getFile().toPath());
            out.write(data);
            saved = true;
        } catch (IOException e) {
            DownloadThread.LOGGER.error("Failed to save cache entry {}", url, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        if (saved) {
            entries.put(url, entry);
            saveIndex();
        }
    }

    public CacheEntry getEntry(String url) {
        return entries.get(url);
    }

    private void loadIndex() {
        if (index.exists()) {
            Map<String, CacheEntry> previousEntries = entries;
            entries = new HashMap<>();
            DataInputStream in = null;
            try {
                in = new DataInputStream(new GZIPInputStream(Files.newInputStream(index.toPath())));
                int length = in.readInt();
                for (int i = 0; i < length; i++) {
                    String url = in.readUTF();
                    String etag = in.readUTF();
                    long time = in.readLong();
                    long expireTime = in.readLong();
                    CacheEntry entry = new CacheEntry(url, etag.length() > 0 ? etag : null, time, expireTime);
                    entries.put(entry.getUrl(), entry);
                }
            } catch (IOException e) {
                DownloadThread.LOGGER.error("Failed to load cache index", e);
                entries = previousEntries;
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }

    private void saveIndex() {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(index.toPath())));
            out.writeInt(entries.size());
            for (Map.Entry<String, CacheEntry> mapEntry : entries.entrySet()) {
                CacheEntry entry = mapEntry.getValue();
                out.writeUTF(entry.getUrl());
                out.writeUTF(entry.getEtag() == null ? "" : entry.getEtag());
                out.writeLong(entry.getTime());
                out.writeLong(entry.getExpireTime());
            }
        } catch (IOException e) {
            DownloadThread.LOGGER.error("Failed to save cache index", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public void deleteEntry(String url) {
        entries.remove(url);
        File file = getFile(url);
        if (file.exists()) {
            file.delete();
        }
    }

    private static File getFile(String url) {
        return new File(DownloadThread.TEXTURE_CACHE.cacheDirectory, Base64.encodeBase64String(url.getBytes()));
    }

    public static class CacheEntry {

        private String url;
        private String etag;
        private long time;
        private long expireTime;

        public CacheEntry(String url, String etag, long time, long expireTime) {
            this.url = url;
            this.etag = etag;
            this.time = time;
            this.expireTime = expireTime;
        }

        public void setEtag(String etag) {
            this.etag = etag;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        public String getUrl() {
            return url;
        }

        public String getEtag() {
            return etag;
        }

        public long getTime() {
            return time;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public File getFile() {
            return TextureCache.getFile(url);
        }
    }
}
