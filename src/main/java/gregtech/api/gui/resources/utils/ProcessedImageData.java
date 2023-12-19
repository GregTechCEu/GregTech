package gregtech.api.gui.resources.utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class ProcessedImageData {

    private final int width;
    private final int height;
    private final Frame[] frames;
    private final long[] delay;
    private final long duration;

    public ProcessedImageData(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        frames = new Frame[] { loadFrom(image) };
        delay = new long[] { 0 };
        duration = 0;
    }

    public ProcessedImageData(GifDecoder decoder) {
        Dimension frameSize = decoder.getFrameSize();
        width = (int) frameSize.getWidth();
        height = (int) frameSize.getHeight();
        frames = new Frame[decoder.getFrameCount()];
        delay = new long[decoder.getFrameCount()];
        long time = 0;
        for (int i = 0; i < decoder.getFrameCount(); i++) {
            frames[i] = loadFrom(decoder.getFrame(i));
            delay[i] = time;
            time += decoder.getDelay(i);
        }
        duration = time;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long[] getDelay() {
        return delay;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isAnimated() {
        return frames.length > 1;
    }

    public int getFrameCount() {
        return frames.length;
    }

    public int uploadFrame(int index) {
        if (index >= 0 && index < frames.length) {
            Frame frame = frames[index];
            if (frame != null) {
                frames[index] = null;
                return uploadFrame(frame.buffer, frame.hasAlpha, width, height);
            }
        }
        return -1;
    }

    private static int uploadFrame(ByteBuffer buffer, boolean hasAlpha, int width, int height) {
        int textureID = GL11.glGenTextures(); // Generate texture ID
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); // Bind texture ID

        // Setup wrap mode
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        // Setup texture scaling filtering
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (!hasAlpha) {
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        }

        // Send texel data to OpenGL
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, hasAlpha ? GL11.GL_RGBA8 : GL11.GL_RGB8, width, height, 0,
                hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);

        // Return the texture ID so we can bind it later again
        return textureID;
    }

    private static Frame loadFrom(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        boolean hasAlpha = false;
        if (image.getColorModel().hasAlpha()) {
            for (int pixel : pixels) {
                if ((pixel >> 24 & 0xFF) < 0xFF) {
                    hasAlpha = true;
                    break;
                }
            }
        }
        int bytesPerPixel = hasAlpha ? 4 : 3;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bytesPerPixel);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
            buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
            buffer.put((byte) (pixel & 0xFF)); // Blue component
            if (hasAlpha) {
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
            }
        }
        buffer.flip();
        return new Frame(buffer, hasAlpha);
    }

    private static class Frame {

        private final ByteBuffer buffer;
        private final boolean hasAlpha;

        public Frame(ByteBuffer buffer, boolean alpha) {
            this.buffer = buffer;
            this.hasAlpha = alpha;
        }
    }
}
