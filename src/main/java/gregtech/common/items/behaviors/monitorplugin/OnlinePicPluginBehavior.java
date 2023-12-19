package gregtech.common.items.behaviors.monitorplugin;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.resources.picturetexture.PictureTexture;
import gregtech.api.gui.resources.utils.DownloadThread;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.behavior.MonitorPluginBaseBehavior;
import gregtech.common.gui.widget.WidgetScrollBar;
import gregtech.common.gui.widget.monitor.WidgetPluginConfig;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OnlinePicPluginBehavior extends MonitorPluginBaseBehavior {

    public String url;
    public float scaleX;
    public float scaleY;
    public float rotation;
    public boolean flippedX;
    public boolean flippedY;

    // run-time
    private String tmpUrl;

    @SideOnly(Side.CLIENT)
    private DownloadThread downloader;
    @SideOnly(Side.CLIENT)
    public PictureTexture texture;
    @SideOnly(Side.CLIENT)
    public boolean failed;
    @SideOnly(Side.CLIENT)
    public String error;

    public void setConfig(String url, float rotation, float scaleX, float scaleY, boolean flippedX, boolean flippedY) {
        if (url.length() > 200 || (this.url.equals(url) && this.rotation == rotation && this.scaleX == scaleX &&
                this.scaleY == scaleY && this.flippedX == flippedX && this.flippedY == flippedY))
            return;
        this.url = url;
        this.rotation = rotation;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.flippedX = flippedX;
        this.flippedY = flippedY;
        writePluginData(GregtechDataCodes.UPDATE_PLUGIN_CONFIG, packetBuffer -> {
            packetBuffer.writeString(url);
            packetBuffer.writeFloat(rotation);
            packetBuffer.writeFloat(scaleX);
            packetBuffer.writeFloat(scaleY);
            packetBuffer.writeBoolean(flippedX);
            packetBuffer.writeBoolean(flippedY);
        });
        markAsDirty();
    }

    @Override
    public void readPluginData(int id, PacketBuffer buf) {
        if (id == GregtechDataCodes.UPDATE_PLUGIN_CONFIG) {
            String _url = buf.readString(200);
            if (!this.url.equals(_url)) {
                this.url = _url;
                this.texture = null;
                this.failed = false;
                this.error = null;
            }
            this.rotation = buf.readFloat();
            this.scaleX = buf.readFloat();
            this.scaleY = buf.readFloat();
            this.flippedX = buf.readBoolean();
            this.flippedY = buf.readBoolean();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString("url", url);
        data.setFloat("rotation", rotation);
        data.setFloat("scaleX", scaleX);
        data.setFloat("scaleY", scaleY);
        data.setBoolean("flippedX", flippedX);
        data.setBoolean("flippedY", flippedY);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.url = data.hasKey("url") ? data.getString("url") : "";
        this.rotation = data.hasKey("rotation") ? data.getFloat("rotation") : 0;
        this.scaleX = data.hasKey("scaleX") ? data.getFloat("scaleX") : 1;
        this.scaleY = data.hasKey("scaleY") ? data.getFloat("scaleY") : 1;
        this.flippedX = data.hasKey("flippedX") && data.getBoolean("flippedX");
        this.flippedY = data.hasKey("flippedY") && data.getBoolean("flippedY");
    }

    @Override
    public MonitorPluginBaseBehavior createPlugin() {
        return new OnlinePicPluginBehavior();
    }

    @Override
    public WidgetPluginConfig customUI(WidgetPluginConfig widgetGroup, IUIHolder holder, EntityPlayer entityPlayer) {
        tmpUrl = url;
        return widgetGroup.setSize(260, 150)
                .widget(new DynamicLabelWidget(20, 20, () -> url.length() > 40 ? (url.substring(0, 39) + "...") : url,
                        0XFFFFFFFF))
                .widget(new TextFieldWidget(20, 30, 175, 10, true, () -> tmpUrl, (text) -> {
                    tmpUrl = text;
                }).setValidator((data) -> true).setMaxStringLength(200))
                .widget(new ClickButtonWidget(200, 30, 45, 10, "confirm",
                        pressed -> setConfig(tmpUrl, this.rotation, this.scaleX, this.scaleY, this.flippedX,
                                this.flippedY)))
                .widget(new WidgetScrollBar(25, 40, 210, -180, 180, 1,
                        value -> setConfig(this.url, value, this.scaleX, this.scaleY, this.flippedX, this.flippedY))
                                .setTitle("rotation", 0XFFFFFFFF).setInitValue(this.rotation))
                .widget(new WidgetScrollBar(25, 60, 210, 0, 1, 0.05f,
                        value -> setConfig(this.url, this.rotation, value, this.scaleY, this.flippedX, this.flippedY))
                                .setTitle("scaleX", 0XFFFFFFFF).setInitValue(this.scaleX))
                .widget(new WidgetScrollBar(25, 80, 210, 0, 1, 0.05f,
                        value -> setConfig(this.url, this.rotation, this.scaleX, value, this.flippedX, this.flippedY))
                                .setTitle("scaleY", 0XFFFFFFFF).setInitValue(this.scaleY))
                .widget(new LabelWidget(40, 115, "flippedX:", 0XFFFFFFFF))
                .widget(new ToggleButtonWidget(90, 110, 20, 20, () -> this.flippedX,
                        state -> setConfig(this.url, this.rotation, this.scaleX, this.scaleY, state, this.flippedY)))
                .widget(new LabelWidget(140, 115, "flippedY:", 0XFFFFFFFF))
                .widget(new ToggleButtonWidget(190, 110, 20, 20, () -> this.flippedY,
                        state -> setConfig(this.url, this.rotation, this.scaleX, this.scaleY, this.flippedX, state)));
    }

    @Override
    public void update() {
        if (this.screen != null && this.screen.getWorld().isRemote) {
            if (this.texture != null) {
                texture.tick(); // gif update
            }
        }
    }

    @Override
    public void renderPlugin(float partialTicks, RayTraceResult rayTraceResult) {
        if (!this.url.isEmpty()) {
            if (texture != null && texture.hasTexture()) {
                texture.render(-0.5f, -0.5f, 1, 1, this.rotation, this.scaleX, this.scaleY, this.flippedX,
                        this.flippedY);
            } else
                this.loadTexture();
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
                    DownloadThread.LOGGER.error("Could not load image of " +
                            (this.screen != null ? this.screen.getPos().toString() : "") + " " + error);
                } else {
                    texture = DownloadThread.loadImage(downloader);
                }
                downloader = null;
            }
        }
    }
}
