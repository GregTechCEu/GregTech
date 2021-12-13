package gregtech.api.gui.resources.utils;

import gregtech.api.gui.resources.picturetexture.AnimatedPictureTexture;
import gregtech.api.gui.resources.picturetexture.OrdinaryTexture;
import gregtech.api.gui.resources.picturetexture.PictureTexture;
import gregtech.api.gui.resources.picturetexture.VideoTexture;
import gregtech.api.util.GTLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class DownloadThread extends Thread {
    public static final Logger LOGGER = GTLog.logger;

    public static final TextureCache TEXTURE_CACHE = new TextureCache();
    public static final DateFormat FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    public static final Object LOCK = new Object();
    public static final int MAXIMUM_ACTIVE_DOWNLOADS = 5;

    public static int activeDownloads = 0;

    public static HashMap<String, PictureTexture> loadedImages = new HashMap<>();
    public static Set<String> loadingImages = new HashSet<>();

    private final String url;

    private ProcessedImageData processedImage;
    private String error;
    private boolean complete;
    private boolean isVideo;

    public DownloadThread(String url) {
        this.url = url;
        synchronized (DownloadThread.LOCK) {
            DownloadThread.loadingImages.add(url);
            DownloadThread.activeDownloads++;
        }
        setName("OPF Download \"" + url + "\"");
        setDaemon(true);
        start();
    }

    public boolean hasFinished() {
        return complete;
    }

    public boolean hasFailed() {
        return hasFinished() && error != null;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public String getError() {
        return error;
    }

    @Override
    public void run() {
        Exception exception = null;
        try {
            byte[] data = load(url);
            String type = ImageUtils.readType(data);
            ByteArrayInputStream in = null;
            try {
                in = new ByteArrayInputStream(data);
                if (type.equalsIgnoreCase("gif")) {
                    GifDecoder gif = new GifDecoder();
                    int status = gif.read(in);
                    if (status == GifDecoder.STATUS_OK) {
                        processedImage = new ProcessedImageData(gif);
                    } else {
                        LOGGER.error("Failed to read gif: {}", status);
                    }
                } else {
                    try {
                        BufferedImage image = ImageIO.read(in);
                        if (image != null) {
                            processedImage = new ProcessedImageData(image);
                        }
                    } catch (IOException e1) {
                        exception = e1;
                        LOGGER.error("Failed to parse BufferedImage from stream", e1);
                    }
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (FoundVideoException e) {
            isVideo = true;
        } catch (Exception e) {
            exception = e;
            LOGGER.error("An exception occurred while loading OPFrame image", e);
        }
        if (!isVideo && processedImage == null) {
            if (exception == null)
                error = "download.exception.gif";
            else if (exception.getMessage() == null)
                error = "download.exception.invalid";
            else if (exception.getMessage().startsWith("Server returned HTTP response code: 403"))
                error = "download.exception.forbidden";
            else if (exception.getMessage().startsWith("Server returned HTTP response code: 404"))
                error = "download.exception.notfound";
            else
                error = "download.exception.invalid";
            TEXTURE_CACHE.deleteEntry(url);
        }
        complete = true;

        synchronized (DownloadThread.LOCK) {
            DownloadThread.loadingImages.remove(url);
            DownloadThread.activeDownloads--;
        }
    }

    public static byte[] load(String url) throws IOException, FoundVideoException {
        TextureCache.CacheEntry entry = TEXTURE_CACHE.getEntry(url);
        long requestTime = System.currentTimeMillis();
        URLConnection connection = new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        int responseCode = -1;
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            if (entry != null) {
                if (entry.getEtag() != null) {
                    httpConnection.setRequestProperty("If-None-Match", entry.getEtag());
                } else if (entry.getTime() != -1) {
                    httpConnection.setRequestProperty("If-Modified-Since", FORMAT.format(new Date(entry.getTime())));
                }
            }
            responseCode = httpConnection.getResponseCode();
        }
        InputStream in = null;
        try {
            in = connection.getInputStream();
            if (!connection.getContentType().startsWith("image"))
                throw new FoundVideoException();
            String etag = connection.getHeaderField("ETag");
            long lastModifiedTimestamp;
            long expireTimestamp = -1;
            String maxAge = connection.getHeaderField("max-age");
            if (maxAge != null && !maxAge.isEmpty()) {
                try {
                    expireTimestamp = requestTime + Long.parseLong(maxAge) * 1000;
                } catch (NumberFormatException ignored) {}
            }
            String expires = connection.getHeaderField("Expires");
            if (expires != null && !expires.isEmpty()) {
                try {
                    expireTimestamp = FORMAT.parse(expires).getTime();
                } catch (ParseException ignored) {}
            }
            String lastModified = connection.getHeaderField("Last-Modified");
            if (lastModified != null && !lastModified.isEmpty()) {
                try {
                    lastModifiedTimestamp = FORMAT.parse(lastModified).getTime();
                } catch (ParseException e) {
                    lastModifiedTimestamp = requestTime;
                }
            } else {
                lastModifiedTimestamp = requestTime;
            }
            if (entry != null) {
                if (etag != null && !etag.isEmpty()) {
                    entry.setEtag(etag);
                }
                entry.setTime(lastModifiedTimestamp);
                if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    File file = entry.getFile();
                    if (file.exists()) {
                        try (FileInputStream fileStream = new FileInputStream(file)) {
                            return IOUtils.toByteArray(fileStream);
                        }

                    }
                }
            }
            byte[] data = IOUtils.toByteArray(in);
            TEXTURE_CACHE.save(url, etag, lastModifiedTimestamp, expireTimestamp, data);
            return data;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static PictureTexture loadImage(DownloadThread thread) {
        PictureTexture texture = null;

        if (!thread.hasFailed()) {
            if (thread.isVideo())
                texture = new VideoTexture(thread.url);
            else if (thread.processedImage.isAnimated())
                texture = new AnimatedPictureTexture(thread.processedImage);
            else
                texture = new OrdinaryTexture(thread.processedImage);
        }
        if (texture != null)
            synchronized (LOCK) {
                loadedImages.put(thread.url, texture);
            }
        return texture;
    }

    public static class FoundVideoException extends Exception {

    }
}
