package gregtech.api.gui.resources;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.picturetexture.AnimatedPictureTexture;
import gregtech.api.gui.resources.picturetexture.OrdinaryTexture;
import gregtech.api.gui.resources.picturetexture.PictureTexture;
import gregtech.api.gui.resources.utils.GifDecoder;
import gregtech.api.gui.resources.utils.ImageUtils;
import gregtech.api.gui.resources.utils.ProcessedImageData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.compress.utils.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileTexture implements IGuiTexture{
    public final File file;
    @SideOnly(Side.CLIENT)
    private PictureTexture texture;
    @SideOnly(Side.CLIENT)
    private ProcessedImageData imageData;
    private Thread downloadThread;
    private boolean failed;

    public FileTexture(File file) {
        this.file = file;
    }

    @SideOnly(Side.CLIENT)
    public void loadFile(){
        if (imageData != null) {
            if (imageData.isAnimated()) {
                texture = new AnimatedPictureTexture(imageData);
                texture.tick();
            } else {
                texture = new OrdinaryTexture(imageData);
            }
            imageData = null;
            downloadThread = null;
        } else if (downloadThread == null) {
            downloadThread = new Thread(() -> {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(file);
                    String type = ImageUtils.readType(inputStream);
                    if (type.equalsIgnoreCase("gif")) {
                        GifDecoder gif = new GifDecoder();
                        inputStream.close();
                        inputStream = new FileInputStream(file);
                        int status = gif.read(inputStream);
                        if (status == GifDecoder.STATUS_OK) {
                            imageData = new ProcessedImageData(gif);
                        } else {
                            failed = true;
                        }
                    } else {
                        inputStream.close();
                        inputStream = new FileInputStream(file);
                        BufferedImage image = ImageIO.read(inputStream);
                        if (image != null) {
                            imageData = new ProcessedImageData(image);
                        } else {
                            failed = true;
                        }
                    }
                } catch (IOException e) {
                    failed = true;
                    texture = null;
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            });
            downloadThread.start();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateTick() {
        if(this.texture != null) {
            texture.tick(); // gif\video update
        }
    }

    @Override
    public void draw(double x, double y, int width, int height) {
        if (texture != null && texture.hasTexture()) {
            texture.render((float)x, (float)y, width, height, 0, 1, 1, false, false);
        } else {
            if (failed || file == null) {
                Minecraft.getMinecraft().fontRenderer.drawString(I18n.format("texture.url_texture.fail"), (int)x + 2, (int)(y + height / 2.0 - 4), 0xffff0000);
            } else {
                this.loadFile();
                int s = (int) Math.floorMod(System.currentTimeMillis() / 200, 24);
                Widget.drawSector((float)(x + width / 2.0), (float)(y + height / 2.0), (float)(Math.min(width, height) / 4.0),
                        0xFF94E2C1, 24, s, s + 5);
            }
        }
    }

}
