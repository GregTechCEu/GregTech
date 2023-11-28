package gregtech.api.gui.resources.utils;

import gregtech.GregTechMod;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageUtils {

    public static final Logger LOGGER = LogManager.getLogger(GregTechMod.class);

    public static String readType(byte[] input) throws IOException {
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(input);
            return readType(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static String readType(InputStream input) throws IOException {
        ImageInputStream stream = ImageIO.createImageInputStream(input);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
        if (!iter.hasNext()) {
            return "";
        }
        ImageReader reader = iter.next();

        if (reader.getFormatName().equalsIgnoreCase("gif"))
            return "gif";

        ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(stream, true, true);
        try {
            reader.read(0, param);
        } catch (IOException e) {
            LOGGER.error("Failed to parse input format", e);
        } finally {
            reader.dispose();
            IOUtils.closeQuietly(stream);
        }
        if (!(input instanceof FileInputStream)) {
            input.reset();
        }
        return reader.getFormatName();
    }
}
