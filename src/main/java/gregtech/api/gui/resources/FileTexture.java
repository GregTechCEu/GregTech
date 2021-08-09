package gregtech.api.gui.resources;

import gregtech.api.gui.resources.picturetexture.AnimatedPictureTexture;
import gregtech.api.gui.resources.picturetexture.OrdinaryTexture;
import gregtech.api.gui.resources.picturetexture.PictureTexture;
import gregtech.api.gui.resources.utils.DownloadThread;
import gregtech.api.gui.resources.utils.GifDecoder;
import gregtech.api.gui.resources.utils.ImageUtils;
import gregtech.api.gui.resources.utils.ProcessedImageData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.compress.utils.IOUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class FileTexture implements IGuiTexture{
    public final File file;
    @SideOnly(Side.CLIENT)
    private PictureTexture texture;
    boolean init;

    public FileTexture(File file) {
        this.file = file;
    }

    @SideOnly(Side.CLIENT)
    public boolean loadFile(){
        init = true;
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
                    texture = new AnimatedPictureTexture(new ProcessedImageData(gif));
                    return true;
                } else {
                    return false;
                }
            } else {
                inputStream.close();
                inputStream = new FileInputStream(file);
                BufferedImage image = ImageIO.read(inputStream);
                if (image != null) {
                    texture = new OrdinaryTexture(new ProcessedImageData(image));
                    return true;
                } else {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return false;
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
        if(this.texture != null) {
            texture.draw(x, y, width, height); // gif\video update
        } else if (!init){
            loadFile();
        }
    }

}
