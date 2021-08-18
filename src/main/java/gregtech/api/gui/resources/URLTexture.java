package gregtech.api.gui.resources;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.picturetexture.PictureTexture;
import gregtech.api.gui.resources.utils.DownloadThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class URLTexture implements IGuiTexture{
    public final String url;
    @SideOnly(Side.CLIENT)
    private DownloadThread downloader;
    @SideOnly(Side.CLIENT)
    private PictureTexture texture;
    @SideOnly(Side.CLIENT)
    private boolean failed;
    @SideOnly(Side.CLIENT)
    private String error;


    public URLTexture(String url) {
        this.url = url;
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
            if (failed || url == null || this.url.equals("")) {
                Minecraft.getMinecraft().fontRenderer.drawString(I18n.format("texture.url_texture.fail"), (int)x + 2, (int)(y + height / 2.0 - 4), 0xffff0000);
            } else {
                this.loadTexture();
                int s = (int) Math.floorMod(System.currentTimeMillis() / 200, 24);
                Widget.drawSector((float)(x + width / 2.0), (float)(y + height / 2.0), (float)(Math.min(width, height) / 4.0),
                        0xFF94E2C1, 24, s, s + 5);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void loadTexture() {
        if (texture == null && !failed) {
            if (downloader == null && DownloadThread.activeDownloads < DownloadThread.MAXIMUM_ACTIVE_DOWNLOADS) {
                PictureTexture loadedTexture = DownloadThread.loadedImages.get(url);
                if (loadedTexture == null) {
                    synchronized (DownloadThread.LOCK) {
                        if (!DownloadThread.loadingImages.contains(url)) {
                            downloader = new DownloadThread(url);
                            return;
                        }
                    }
                } else {
                    texture = loadedTexture;
                }
            }
            if (downloader != null && downloader.hasFinished()) {
                if (downloader.hasFailed()) {
                    failed = true;
                    error = downloader.getError();
                    DownloadThread.LOGGER.error("Could not load image of " + url + " : " + error);
                } else {
                    texture = DownloadThread.loadImage(downloader);
                }
                downloader = null;
            }
        }
    }
}
