package gregtech.api.gui.resources.picturetexture;

import gregtech.api.gui.resources.utils.ProcessedImageData;

import net.minecraft.client.renderer.GlStateManager;

import java.util.Arrays;

public class AnimatedPictureTexture extends PictureTexture {

    private final int[] textureIDs;
    private final long[] delay;
    private final long duration;

    private int completedFrames;
    private ProcessedImageData imageData;

    public AnimatedPictureTexture(ProcessedImageData image) {
        super(image.getWidth(), image.getHeight());
        imageData = image;
        textureIDs = new int[image.getFrameCount()];
        delay = image.getDelay();
        duration = image.getDuration();
        Arrays.fill(textureIDs, -1);
    }

    @Override
    public void tick() {
        if (imageData != null) {
            long startTime = System.currentTimeMillis();
            int index = 0;
            while (completedFrames < textureIDs.length && index < textureIDs.length &&
                    System.currentTimeMillis() - startTime < 10) {
                while (textureIDs[index] != -1 && index < textureIDs.length - 1)
                    index++;
                if (textureIDs[index] == -1)
                    textureIDs[index] = uploadFrame(index);
            }
        }
    }

    @Override
    public int getTextureID() {
        long time = duration > 0 ? System.currentTimeMillis() % duration : 0;
        int index = 0;
        for (int i = 0; i < delay.length; i++) {
            if (delay[i] >= time) {
                index = i;
                break;
            }
        }
        return textureIDs[index];
    }

    private int uploadFrame(int index) {
        int id = imageData.uploadFrame(index);
        textureIDs[index] = id;
        if (++completedFrames >= imageData.getFrameCount()) {
            imageData = null;
        }
        return id;
    }

    @Override
    public void release() {
        for (int textureID : textureIDs) GlStateManager.deleteTexture(textureID);
    }
}
