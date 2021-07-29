package gregtech.api.gui.resources.onlinepictexture;

import gregtech.api.gui.resources.onlinepic.ProcessedImageData;

public class OrdinaryTexture extends PictureTexture {

    private final int textureID;

    public OrdinaryTexture(ProcessedImageData image) {
        super(image.getWidth(), image.getHeight());
        textureID = image.uploadFrame(0);
    }

    @Override
    public void tick() {
    }

    @Override
    public int getTextureID() {
        return textureID;
    }
}

